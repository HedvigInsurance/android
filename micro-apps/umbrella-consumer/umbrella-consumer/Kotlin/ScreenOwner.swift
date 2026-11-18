import Foundation
@preconcurrency import HedvigShared
import KMPNativeCoroutinesAsync
import Observation
import os

/// What a screen shows, built from the Kotlin state its screen model publishes.
protocol ScreenViewState: Equatable {
    associatedtype KotlinState: AnyObject
    init(_ kotlinState: KotlinState)
    /// Names the state for the log without printing member data.
    var logDescription: String { get }
}

private let screenLog = Logger(subsystem: Bundle.main.bundleIdentifier ?? "ScreenOwner", category: "screen")

/// Owns one Kotlin screen model for as long as the screen exists, following `ScreenModel`'s rules: seeds `state` from
/// the model's current state, follows every later one on the main actor, and cancels and closes when released.
///
/// The collection loop must run until the stream ends and never `break` or `return` early. An item the loop has not
/// asked past keeps the Kotlin collection suspended, and neither cancelling the task nor `close()` ends it.
@MainActor
@Observable
final class ScreenOwner<Model, ViewState: ScreenViewState> where Model: ScreenModel<ViewState.KotlinState> {
    private(set) var state: ViewState
    /// True from the first item of `states` until the collection ends. Kotlin's collection is running exactly then,
    /// so events sent while it is false wait in a small buffer that can overflow and drop the newest ones.
    private(set) var isCollecting = false

    @ObservationIgnored let model: Model
    @ObservationIgnored private let teardown: ScreenModelTeardown<ViewState.KotlinState>

    init(_ model: Model) {
        self.model = model
        state = ViewState(model.currentState)
        teardown = ScreenModelTeardown(model: model, name: "\(Model.self)")
        screenLog.debug("\(Model.self, privacy: .public) owner seeded: \(self.state.logDescription, privacy: .public)")
        let states = model.states
        teardown.collection = Task { [weak self] in
            do {
                for try await kotlinState in asyncSequence(for: states) {
                    self?.receive(kotlinState)
                }
                self?.isCollecting = false
            } catch is CancellationError {
            } catch {
                self?.isCollecting = false
                let nsError = error as NSError
                let kotlinException = nsError.userInfo["KotlinException"].map { "\(type(of: $0))" } ?? "none"
                screenLog.error(
                    "\(Model.self, privacy: .public) state collection failed: domain \(nsError.domain, privacy: .public), Kotlin exception \(kotlinException, privacy: .public)"
                )
            }
        }
    }

    private func receive(_ kotlinState: ViewState.KotlinState) {
        if !isCollecting {
            isCollecting = true
        }
        let next = ViewState(kotlinState)
        guard next != state else {
            screenLog.debug("\(Model.self, privacy: .public) owner state unchanged: \(next.logDescription, privacy: .public)")
            return
        }
        state = next
        screenLog.debug("\(Model.self, privacy: .public) owner state: \(next.logDescription, privacy: .public)")
    }

    /// Runs `event` only while `states` is collected; otherwise drops it and logs `name`, never the event's values.
    func sendWhileCollecting(_ name: String, _ event: () -> Void) {
        guard isCollecting else {
            screenLog.info("\(Model.self, privacy: .public) not collecting, \(name, privacy: .public) not sent")
            return
        }
        event()
    }

    // Not isolated: the deployment target is iOS 17.0. SwiftUI releases the owner on the main thread, so the model
    // is closed before deinit returns; a release elsewhere closes it on the next main queue turn.
    deinit {
        teardown.run()
    }
}

extension ScreenOwner {
    /// Calls `navigate` with the request's target only when this call takes the request, so each Kotlin navigation
    /// request is performed once however often the state carrying it is observed.
    func perform<Target: AnyObject>(_ request: NavigationRequest<Target>?, _ navigate: (Target) -> Void) {
        guard let request, request.tryConsume() else { return }
        navigate(request.target)
    }
}

/// Cancels a screen model's state collection and closes the model on the main thread, which `close()` requires. The
/// owner sets it up on the main actor and only its `deinit` uses it again, so it is never used from two threads at once.
private final class ScreenModelTeardown<KotlinState: AnyObject>: @unchecked Sendable {
    private let model: ScreenModel<KotlinState>
    private let name: String
    var collection: Task<Void, Never>?

    init(model: ScreenModel<KotlinState>, name: String) {
        self.model = model
        self.name = name
    }

    func run() {
        screenLog.debug("\(self.name, privacy: .public) owner deinit")
        collection?.cancel()
        if Thread.isMainThread {
            model.close()
        } else {
            DispatchQueue.main.async { self.model.close() }
        }
    }
}

/// The value picked by a Kotlin `handle` call, which takes one closure per case and calls exactly one of them.
/// Every closure passes its value to `pick`.
func oneOf<Value>(_ cases: (_ pick: @escaping (Value) -> Void) -> Void) -> Value {
    var picked: Value?
    cases { picked = $0 }
    guard let picked else { preconditionFailure("No case picked a value") }
    return picked
}

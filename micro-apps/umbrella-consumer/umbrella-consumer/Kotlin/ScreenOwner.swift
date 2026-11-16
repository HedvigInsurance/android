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

    @ObservationIgnored let model: Model
    @ObservationIgnored private var collection: Task<Void, Never>?

    init(_ model: Model) {
        self.model = model
        state = ViewState(model.currentState)
        screenLog.debug("\(Model.self, privacy: .public) owner seeded: \(self.state.logDescription, privacy: .public)")
        let states = model.states
        collection = Task { [weak self] in
            do {
                for try await kotlinState in asyncSequence(for: states) {
                    self?.receive(kotlinState)
                }
            } catch is CancellationError {
            } catch {
                let nsError = error as NSError
                let kotlinException = nsError.userInfo["KotlinException"].map { "\(type(of: $0))" } ?? "none"
                screenLog.error(
                    "\(Model.self, privacy: .public) state collection failed: domain \(nsError.domain, privacy: .public), Kotlin exception \(kotlinException, privacy: .public)"
                )
            }
        }
    }

    private func receive(_ kotlinState: ViewState.KotlinState) {
        let next = ViewState(kotlinState)
        guard next != state else {
            screenLog.debug("\(Model.self, privacy: .public) owner state unchanged: \(next.logDescription, privacy: .public)")
            return
        }
        state = next
        screenLog.debug("\(Model.self, privacy: .public) owner state: \(next.logDescription, privacy: .public)")
    }

    // Runs on the main actor; verified on iOS 27.2 only, while the deployment target is 17.0.
    isolated deinit {
        screenLog.debug("\(Model.self, privacy: .public) owner deinit")
        collection?.cancel()
        model.close()
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

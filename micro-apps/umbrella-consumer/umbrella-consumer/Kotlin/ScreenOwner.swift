@preconcurrency import HedvigShared
@preconcurrency import KMPNativeCoroutinesAsync
import Observation

/// What a screen shows, built from the Kotlin state its screen model publishes.
protocol ScreenViewState: Equatable {
    associatedtype KotlinState: AnyObject
    init(_ kotlinState: KotlinState)
    /// Names the state for the log without printing member data.
    var logDescription: String { get }
}

/// Owns one Kotlin screen model for as long as the screen exists, following `ScreenModel`'s rules: seeds `state` from
/// the model's current state, follows every later one on the main actor, and cancels and closes when released.
@MainActor
@Observable
final class ScreenOwner<Model, ViewState: ScreenViewState> where Model: ScreenModel<ViewState.KotlinState> {
    private(set) var state: ViewState

    @ObservationIgnored let model: Model
    @ObservationIgnored private var collection: Task<Void, Never>?

    init(_ model: Model) {
        self.model = model
        state = ViewState(model.currentState)
        ShellLog.info("\(Model.self) owner seeded: \(state.logDescription)")
        let states = model.states
        collection = Task { [weak self] in
            do {
                for try await kotlinState in asyncSequence(for: states) {
                    self?.receive(kotlinState)
                }
            } catch is CancellationError {
            } catch {
                ShellLog.error("\(Model.self) state collection failed: \(type(of: error))")
            }
        }
    }

    private func receive(_ kotlinState: ViewState.KotlinState) {
        #if DEBUG
        dispatchPrecondition(condition: .onQueue(.main))
        #endif
        let next = ViewState(kotlinState)
        guard next != state else {
            ShellLog.info("\(Model.self) owner state unchanged: \(next.logDescription)")
            return
        }
        state = next
        ShellLog.info("\(Model.self) owner state: \(state.logDescription)")
    }

    isolated deinit {
        ShellLog.info("\(Model.self) owner deinit")
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

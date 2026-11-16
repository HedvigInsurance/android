#if DEBUG
@preconcurrency import HedvigShared
#endif
import SwiftUI

struct RootView: View {
    @Environment(AppModel.self) private var app

    var body: some View {
        if app.isLoggedIn {
            NavigationStack {
                List {
                    NavigationLink("Claim history") {
                        claimHistoryDestination()
                    }
                    #if DEBUG
                    Section("Bridge checks") {
                        Button("Throw undeclared") {
                            ShellLog.info("Bridge check: calling throwForBridgeCheck")
                            app.holder.throwForBridgeCheck()
                            ShellLog.info("Bridge check: throwForBridgeCheck returned")
                        }
                        Button("Throw declared") {
                            ShellLog.info("Bridge check: calling throwDeclaredForBridgeCheck")
                            do {
                                try app.holder.throwDeclaredForBridgeCheck()
                                ShellLog.info("Bridge check: throwDeclaredForBridgeCheck returned")
                            } catch {
                                ShellLog.info("Bridge check: throwDeclaredForBridgeCheck threw \(ShellDebug.describe(error))")
                            }
                        }
                        if ShellDebug.claimHistorySample {
                            NavigationLink("Sample content") {
                                ClaimHistorySampleView(
                                    state: ClaimHistoryViewState.mapped(ShellDebug.claimHistorySampleState())
                                )
                            }
                        }
                    }
                    #endif
                    Section {
                        Button("Log out", role: .destructive) {
                            app.logOut()
                        }
                    }
                }
                .navigationTitle("Umbrella consumer")
            }
        } else {
            LoginView(app: app)
        }
    }

    @ViewBuilder
    private func claimHistoryDestination() -> some View {
        ClaimHistoryView(holder: app.holder)
    }
}

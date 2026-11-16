@preconcurrency import HedvigShared
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
                            ShellLog.info("Calling throwForBridgeCheck()")
                            app.holder.throwForBridgeCheck()
                        }
                        Button("Throw declared") {
                            ShellLog.info("Calling throwDeclaredForBridgeCheck()")
                            do {
                                try app.holder.throwDeclaredForBridgeCheck()
                            } catch {
                                ShellLog.info("throwDeclaredForBridgeCheck() threw \(type(of: error))")
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

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
                    NavigationLink("Payments") {
                        PaymentOverviewView(holder: app.holder)
                    }
                    #if DEBUG
                    if ShellDebug.claimHistorySample {
                        Section("Debug") {
                            NavigationLink("Sample content") {
                                ClaimHistorySampleView(
                                    state: ClaimHistoryViewState(ShellDebug.claimHistorySampleState())
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

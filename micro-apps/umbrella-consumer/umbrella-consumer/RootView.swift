import SwiftUI

struct RootView: View {
    @Environment(AppModel.self) private var app
    @State private var travelFlow = TravelCertificateFlow()

    var body: some View {
        if app.isLoggedIn {
            NavigationStack(path: $travelFlow.path) {
                List {
                    NavigationLink("Claim history") {
                        claimHistoryDestination()
                    }
                    NavigationLink("Payments") {
                        PaymentOverviewView(holder: app.holder)
                    }
                    NavigationLink("Travel certificate", value: TravelRoute.history)
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
                .navigationDestination(for: TravelRoute.self) { route in
                    TravelCertificateDestination(route: route, holder: app.holder, flow: travelFlow)
                }
            }
        } else {
            LoginView(app: app)
                .onAppear {
                    // A later login starts at the root, not on the travel screens left open at logout.
                    travelFlow = TravelCertificateFlow()
                }
        }
    }

    @ViewBuilder
    private func claimHistoryDestination() -> some View {
        ClaimHistoryView(holder: app.holder)
    }
}

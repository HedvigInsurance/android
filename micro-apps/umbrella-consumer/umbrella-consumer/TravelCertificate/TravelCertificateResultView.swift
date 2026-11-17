import SwiftUI

/// The end of the flow: a certificate exists. Only the signed URL's host and length are shown.
struct TravelCertificateResultView: View {
    let summary: TravelCreatedSummary
    let flow: TravelCertificateFlow

    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "checkmark.seal")
                .font(.largeTitle)
            Text("A travel certificate was created")
                .font(.headline)
            Text("Link host: \(summary.host)")
            Text("Link length: \(summary.length) characters")
                .monospacedDigit()
            Button("Back to history") {
                flow.backToHistory()
            }
            .buttonStyle(.borderedProminent)
        }
        .padding()
        .navigationTitle("Certificate created")
    }
}

#Preview {
    NavigationStack {
        TravelCertificateResultView(
            summary: TravelCreatedSummary(host: "example.com", length: 412),
            flow: TravelCertificateFlow()
        )
    }
}

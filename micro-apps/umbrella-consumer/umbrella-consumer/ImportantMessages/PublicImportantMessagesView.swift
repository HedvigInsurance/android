@preconcurrency import HedvigShared
import SwiftUI

/// The important messages for people who are not logged in, one card per message. Loads them again whenever the app
/// becomes active after the first time.
struct PublicImportantMessagesView: View {
    let holder: IosGraphHolder
    @State private var owner: PublicImportantMessagesScreenOwner?
    @State private var hasBeenActive = false
    @Environment(\.scenePhase) private var scenePhase
    @Environment(\.openURL) private var openURL

    var body: some View {
        VStack(spacing: 8) {
            ForEach(owner?.state.messages ?? []) { row in
                PublicImportantMessageCard(row: row, open: { openURL($0) })
            }
        }
        .onAppear {
            if owner == nil {
                owner = PublicImportantMessagesScreenOwner(holder.publicImportantMessagesScreenModel())
            }
        }
        .onChange(of: scenePhase, initial: true) { _, phase in
            guard phase == .active else { return }
            if hasBeenActive {
                owner?.reload()
            } else {
                hasBeenActive = true
            }
        }
    }
}

private struct PublicImportantMessageCard: View {
    let row: PublicImportantMessageRow
    let open: (URL) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(row.message)
            if let link = row.link {
                Button(link.buttonText) {
                    open(link.url)
                }
                .buttonStyle(.bordered)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(12)
        .background(color.opacity(0.15), in: RoundedRectangle(cornerRadius: 12))
    }

    private var color: Color {
        switch row.severity {
        case .info: .blue
        case .warning: .orange
        case .error: .red
        }
    }
}

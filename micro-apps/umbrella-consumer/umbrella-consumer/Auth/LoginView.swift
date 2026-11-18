@preconcurrency import HedvigShared
import SwiftUI

struct LoginView: View {
    @State private var model: LoginModel
    @Environment(\.scenePhase) private var scenePhase
    private let holder: IosGraphHolder

    init(app: AppModel) {
        _model = State(initialValue: LoginModel(app: app))
        holder = app.holder
    }

    var body: some View {
        VStack(spacing: 24) {
            PublicImportantMessagesView(holder: holder)
            Text("Log in with BankID")
                .font(.title2)
            content
                .frame(width: 280, height: 280)
            Button("New code") {
                model.start()
            }
            .buttonStyle(.bordered)
        }
        .padding()
        .onAppear {
            if scenePhase == .active {
                model.start()
            }
        }
        .onDisappear { model.cancel() }
        .onChange(of: scenePhase) { _, phase in
            if phase == .active {
                model.start()
            } else {
                model.cancel()
            }
        }
    }

    @ViewBuilder
    private var content: some View {
        switch model.state {
        case .starting:
            ProgressView()
        case let .showingQr(data):
            if let image = QrCodeImage.make(data, side: 840) {
                Image(uiImage: image)
                    .interpolation(.none)
                    .resizable()
                    .scaledToFit()
                    .accessibilityLabel("BankID QR code")
            } else {
                Text("Could not draw the QR code")
            }
        case .exchanging:
            ProgressView("Logging in")
        case let .failed(message):
            Text(message)
                .multilineTextAlignment(.center)
        }
    }
}

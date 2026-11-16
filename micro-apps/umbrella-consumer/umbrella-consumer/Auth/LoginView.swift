import SwiftUI

struct LoginView: View {
    @State private var model: LoginModel

    init(app: AppModel) {
        _model = State(initialValue: LoginModel(app: app))
    }

    var body: some View {
        VStack(spacing: 24) {
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
        .onAppear { model.start() }
        .onDisappear { model.cancel() }
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

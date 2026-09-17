import SwiftUI
import SharedApp

@main
struct KMPTemplateApp: App {
    var body: some Scene {
        WindowGroup { ComposeRoot().ignoresSafeArea() }
    }
}

struct ComposeRoot: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }
    func updateUIViewController(_ controller: UIViewController, context: Context) {}
}

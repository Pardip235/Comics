import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        KoinIOS.shared.doInitKoinIOS()
    }
    
    var body: some Scene {
        WindowGroup {
            MainTabView()
        }
    }
}

struct MainTabView: View {
    var body: some View {
        TabView {
            ComicsScreen()
                .tabItem {
                    Label("Comics", systemImage: "book.fill")
                }
            
            FavoritesScreen()
                .tabItem {
                    Label("Favorites", systemImage: "heart.fill")
                }
        }
    }
}
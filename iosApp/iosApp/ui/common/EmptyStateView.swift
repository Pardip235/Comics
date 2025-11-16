import SwiftUI

struct EmptyStateView: View {
    let message: String
    
    var body: some View {
        VStack {
            Image(systemName: "tray")
                .font(.system(size: 50))
                .foregroundColor(.gray)
            Text(message)
                .font(.headline)
                .foregroundColor(.secondary)
        }
    }
}


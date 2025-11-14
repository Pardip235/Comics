import SwiftUI
import Shared

struct ContentView: View {
    @State private var showContent = false
    private let greeting = Greeting().greet()
    
    var body: some View {
        VStack(spacing: 20) {
            Button(action: {
                withAnimation {
                    showContent.toggle()
                }
            }) {
                Text("Click me!")
                    .font(.headline)
                    .foregroundColor(.white)
                    .padding()
                    .background(Color.blue)
                    .cornerRadius(10)
            }
            
            if showContent {
                Text(greeting)
                    .font(.title2)
                    .padding()
                    .transition(.opacity)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color(.systemBackground))
    }
}

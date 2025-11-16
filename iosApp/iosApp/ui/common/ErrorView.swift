import SwiftUI
import Shared

struct ErrorView: View {
    let errorType: ErrorType
    let onRetry: () -> Void
    
    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "exclamationmark.triangle")
                .font(.system(size: 50))
                .foregroundColor(.orange)
            
            Text(errorMessage)
                .font(.headline)
                .multilineTextAlignment(.center)
            
            Button("Retry", action: onRetry)
                .buttonStyle(.borderedProminent)
        }
        .padding()
    }
    
    private var errorMessage: String {
        if errorType == ErrorType.networkError {
            return "Network error. Please check your connection."
        } else {
            return "Something went wrong. Please try again."
        }
    }
}


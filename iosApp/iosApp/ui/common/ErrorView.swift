import SwiftUI
import Shared
import os.log

struct ErrorView: View {
    let errorType: ErrorType
    let onRetry: () -> Void
    
    private let logger = Logger(subsystem: "com.bpn.comics", category: "ErrorView")
    
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
        .onAppear {
            // Log error to Xcode console
            logger.error("Error displayed: \(errorType.name, privacy: .public)")
        }
    }
    
    private var errorMessage: String {
        switch errorType {
        case .networkError:
            return "Network error. Please check your connection."
        case .unknownError:
            return "Something went wrong. Please try again.\n\nCheck Xcode console (View > Debug Area > Activate Console) for details."
        default:
            return "An error occurred. Please try again."
        }
    }
}


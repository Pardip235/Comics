import Foundation
import Shared
import SwiftUI
import os.log

@MainActor
class FavoritesViewModelWrapper: ObservableObject {
    @Published var uiState: FavoritesUiState
    
    private let viewModel: FavoritesViewModel
    private var cancellable: Cancellable?
    private let logger = Logger(subsystem: "com.bpn.comics", category: "FavoritesViewModel")
    
    init() {
        viewModel = KoinIOS.shared.getFavoritesViewModel()
        
        // Get initial state
        uiState = StateFlowExtensionsKt.getValue(viewModel.uiState) as! FavoritesUiState
        
        // Observe state changes
        let scope = KoinIOS.shared.getScope()
        cancellable = StateFlowExtensionsKt.observe(
            viewModel.uiState,
            scope: scope
        ) { [weak self] newState in
            Task { @MainActor in
                let newState = newState as! FavoritesUiState
                self?.uiState = newState
                
                // Log errors to Xcode console
                if let errorType = newState.errorType {
                    self?.logger.error("Error in FavoritesViewModel: \(errorType.name, privacy: .public)")
                }
            }
        }
    }
    
    deinit {
        cancellable?.cancel()
    }
    
    func toggleFavorite(comicNumber: Int32) {
        viewModel.toggleFavorite(comicNumber: comicNumber)
    }
    
    func retry() {
        viewModel.retry()
    }
    
    func refresh() {
        viewModel.refresh()
    }
}


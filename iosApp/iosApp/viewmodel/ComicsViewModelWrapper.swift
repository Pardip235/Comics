import Foundation
import Shared
import SwiftUI
import os.log

@MainActor
class ComicsViewModelWrapper: ObservableObject {
    @Published var uiState: ComicsUiState
    
    private let viewModel: ComicsViewModel
    private var cancellable: Cancellable?
    private let logger = Logger(subsystem: "com.bpn.comics", category: "ComicsViewModel")
    
    init() {
        viewModel = KoinIOS.shared.getComicsViewModel()
        
        // Get initial state
        uiState = StateFlowExtensionsKt.getValue(viewModel.uiState) as! ComicsUiState
        
        // Observe state changes
        let scope = KoinIOS.shared.getScope()
        cancellable = StateFlowExtensionsKt.observe(
            viewModel.uiState,
            scope: scope
        ) { [weak self] newState in
            Task { @MainActor in
                let newState = newState as! ComicsUiState
                self?.uiState = newState
                
                // Log errors to Xcode console
                if let errorType = newState.errorType {
                    self?.logger.error("Error in ComicsViewModel: \(errorType.name, privacy: .public)")
                }
            }
        }
    }
    
    deinit {
        cancellable?.cancel()
    }
    
    func refresh() {
        viewModel.refresh()
    }
    
    func toggleFavorite(comicNumber: Int32) {
        viewModel.toggleFavorite(comicNumber: comicNumber)
    }
    
    func loadMoreComics() {
        viewModel.loadMoreComics()
    }
    
    func retry() {
        viewModel.retry()
    }
}


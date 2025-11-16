import Foundation
import Shared
import SwiftUI

@MainActor
class ComicsViewModelWrapper: ObservableObject {
    @Published var uiState: ComicsUiState
    
    private let viewModel: ComicsViewModel
    private var cancellable: Cancellable?
    
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
                self?.uiState = newState as! ComicsUiState
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


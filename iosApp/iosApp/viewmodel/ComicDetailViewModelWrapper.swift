import Foundation
import Shared
import SwiftUI

@MainActor
class ComicDetailViewModelWrapper: ObservableObject {
    @Published var uiState: ComicDetailUiState
    
    private let viewModel: ComicDetailViewModel
    private var cancellable: Cancellable?
    
    init(comicNumber: Int32) {
        viewModel = KoinIOS.shared.getComicDetailViewModel()
        
        // Get initial state
        uiState = StateFlowExtensionsKt.getValue(viewModel.uiState) as! ComicDetailUiState
        
        // Observe state changes
        let scope = KoinIOS.shared.getScope()
        cancellable = StateFlowExtensionsKt.observe(
            viewModel.uiState,
            scope: scope
        ) { [weak self] newState in
            Task { @MainActor in
                self?.uiState = newState as! ComicDetailUiState
            }
        }
        
        // Load comic detail
        viewModel.loadComicDetail(comicNumber: comicNumber)
    }
    
    deinit {
        cancellable?.cancel()
    }
    
    func toggleFavorite() {
        viewModel.toggleFavorite()
    }
    
    func retry(comicNumber: Int32) {
        viewModel.retry(comicNumber: comicNumber)
    }
    
    func clearError() {
        viewModel.clearError()
    }
}


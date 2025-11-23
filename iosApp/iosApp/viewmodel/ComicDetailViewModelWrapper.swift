import Foundation
import Shared
import SwiftUI
import os.log

@MainActor
class ComicDetailViewModelWrapper: ObservableObject {
    @Published var uiState: ComicDetailUiState
    
    private let viewModel: ComicDetailViewModel
    private var cancellable: Cancellable?
    private let logger = Logger(subsystem: "com.bpn.comics", category: "ComicDetailViewModel")
    
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
                let newState = newState as! ComicDetailUiState
                self?.uiState = newState
                
                // Log errors to Xcode console
                if let errorType = newState.errorType {
                    self?.logger.error("Error in ComicDetailViewModel: \(errorType.name, privacy: .public)")
                }
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
    
    func retry() {
        viewModel.retry()
    }
    
    func clearError() {
        viewModel.clearError()
    }
}


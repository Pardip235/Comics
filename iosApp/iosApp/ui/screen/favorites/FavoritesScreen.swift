import SwiftUI
import Shared

struct FavoritesScreen: View {
    @StateObject private var viewModel = FavoritesViewModelWrapper()
    
    var body: some View {
        NavigationStack {
            ZStack {
                if viewModel.uiState.isLoading && viewModel.uiState.favorites.isEmpty {
                    LoadingView(message: "Loading favorites...")
                } else if let errorType = viewModel.uiState.errorType {
                    ErrorView(
                        errorType: errorType,
                        onRetry: { viewModel.retry() }
                    )
                } else if viewModel.uiState.favorites.isEmpty {
                    EmptyStateView(message: "No favorite comics yet")
                } else {
                    ScrollView {
                        LazyVStack(spacing: 16) {
                            ForEach(Array(viewModel.uiState.favorites.enumerated()), id: \.element.num) { _, comic in
                                NavigationLink(
                                    destination: ComicDetailScreen(comicNumber: Int32(comic.num))
                                ) {
                                    ComicCard(comic: comic) {
                                        viewModel.toggleFavorite(comicNumber: Int32(comic.num))
                                    }
                                }
                            }
                        }
                        .padding(16)
                    }
                }
            }
            .navigationTitle("Favorites")
        }
    }
}


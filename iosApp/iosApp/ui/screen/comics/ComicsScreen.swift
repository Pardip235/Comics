import SwiftUI
import Shared

struct ComicsScreen: View {
    @StateObject private var viewModel = ComicsViewModelWrapper()
    
    var body: some View {
        NavigationStack {
            ZStack {
                if viewModel.uiState.isLoading && viewModel.uiState.comics.isEmpty {
                    LoadingView(message: "Loading comics...")
                } else if let errorType = viewModel.uiState.errorType {
                    ErrorView(
                        errorType: errorType,
                        onRetry: { viewModel.retry() }
                    )
                } else if viewModel.uiState.comics.isEmpty {
                    EmptyStateView(message: "No comics available")
                } else {
                    ScrollView {
                        LazyVStack(spacing: 16) {
                            // Show refresh indicator at top when refreshing
                            if viewModel.uiState.isRefreshing {
                                HStack {
                                    Spacer()
                                    ProgressView()
                                        .scaleEffect(0.8)
                                    Text("Refreshing...")
                                        .font(.caption)
                                        .foregroundColor(.secondary)
                                    Spacer()
                                }
                                .padding(.vertical, 8)
                            }
                            
                            ForEach(Array(viewModel.uiState.comics.enumerated()), id: \.element.num) { index, comic in
                                NavigationLink(
                                    destination: ComicDetailScreen(comicNumber: Int32(comic.num))
                                ) {
                                    ComicCard(comic: comic) {
                                        viewModel.toggleFavorite(comicNumber: Int32(comic.num))
                                    }
                                }
                                .onAppear {
                                    // Auto-load more when user scrolls near the end (within 3 items)
                                    if index >= viewModel.uiState.comics.count - 3 &&
                                       viewModel.uiState.hasMore &&
                                       !viewModel.uiState.isLoadingMore {
                                        viewModel.loadMoreComics()
                                    }
                                }
                            }
                            
                            // Show loading indicator at bottom when loading more
                            if viewModel.uiState.isLoadingMore {
                                ProgressView()
                                    .frame(maxWidth: .infinity)
                                    .padding()
                            }
                        }
                        .padding()
                    }
                }
            }
            .navigationTitle("Comics")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: { viewModel.refresh() }) {
                        Group {
                            if viewModel.uiState.isRefreshing {
                                ProgressView()
                                    .progressViewStyle(CircularProgressViewStyle(tint: .blue))
                                    .frame(width: 24, height: 24)
                            } else {
                                Image(systemName: "arrow.clockwise")
                            }
                        }
                    }
                    .disabled(viewModel.uiState.isLoading || viewModel.uiState.isRefreshing)
                }
            }
        }
    }
}


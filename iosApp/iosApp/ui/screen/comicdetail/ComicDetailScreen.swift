import SwiftUI
import Shared

struct ComicDetailScreen: View {
    let comicNumber: Int32
    @StateObject private var viewModel: ComicDetailViewModelWrapper
    
    init(comicNumber: Int32) {
        self.comicNumber = comicNumber
        _viewModel = StateObject(wrappedValue: ComicDetailViewModelWrapper(comicNumber: comicNumber))
    }
    
    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                if viewModel.uiState.isLoading {
                    LoadingView(message: "Loading comic...")
                        .frame(maxWidth: .infinity, minHeight: 400)
                } else if viewModel.uiState.errorType != nil {
                    ErrorView(
                        errorType: viewModel.uiState.errorType!,
                        onRetry: { viewModel.retry(comicNumber: comicNumber) }
                    )
                } else if let comic = viewModel.uiState.comic {
                    ComicDetailContent(comic: comic)
                } else {
                    EmptyStateView(message: "Comic not found")
                }
            }
            .padding()
        }
        .navigationTitle("Comic #\(comicNumber)")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            if viewModel.uiState.comic != nil {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: { viewModel.toggleFavorite() }) {
                        Image(
                            systemName: viewModel.uiState.comic?.isFavorite == true
                                ? "heart.fill"
                                : "heart"
                        )
                        .foregroundColor(
                            viewModel.uiState.comic?.isFavorite == true ? .red : .gray
                        )
                    }
                }
            }
        }
    }
}

struct ComicDetailContent: View {
    let comic: Comic
    
    var body: some View {
        VStack(alignment: .center, spacing: 16) {
            // Comic Image - full width, 1:1 aspect ratio, centered
            AsyncImage(url: URL(string: comic.img)) { phase in
                switch phase {
                case .empty:
                    ProgressView()
                        .frame(maxWidth: .infinity)
                        .aspectRatio(1, contentMode: .fit)
                case .success(let image):
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                case .failure:
                    VStack {
                        Text("Image unavailable")
                            .font(.body)
                            .foregroundColor(.secondary)
                    }
                    .frame(maxWidth: .infinity)
                    .aspectRatio(1, contentMode: .fit)
                    .background(Color(.systemGray5))
                @unknown default:
                    EmptyView()
                }
            }
            .aspectRatio(1, contentMode: .fit)
            .frame(maxWidth: .infinity)
            
            // Comic Title - centered
            Text(comic.title)
                .font(.title2)
                .fontWeight(.bold)
                .multilineTextAlignment(.center)
            
            // Comic Number and Date - centered
            Text("Comic #\(comic.num) • \(comic.month)/\(comic.day)/\(comic.year)")
                .font(.body)
                .foregroundColor(.secondary)
            
            // Alt Text - in a card
            if !comic.alt.isEmpty {
                VStack(alignment: .leading, spacing: 8) {
                    Text("Alt Text")
                        .font(.caption)
                        .fontWeight(.bold)
                        .foregroundColor(.secondary)
                    Text(comic.alt)
                        .font(.body)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(16)
                .background(Color(.systemGray6))
                .cornerRadius(12)
            }
            
            // Transcript - in a card
            if !comic.transcript.isEmpty {
                VStack(alignment: .leading, spacing: 8) {
                    Text("Transcript")
                        .font(.caption)
                        .fontWeight(.bold)
                        .foregroundColor(.secondary)
                    Text(comic.transcript)
                        .font(.body)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(16)
                .background(Color(.systemGray6))
                .cornerRadius(12)
            }
        }
        .padding(16)
    }
}


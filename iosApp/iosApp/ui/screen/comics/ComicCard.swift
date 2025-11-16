import SwiftUI
import Shared

struct ComicCard: View {
    let comic: Comic
    let onFavoriteToggle: () -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Image - full width, centered
            ZStack {
                AsyncImage(url: URL(string: comic.img)) { phase in
                    switch phase {
                    case .empty:
                        ProgressView()
                            .frame(height: 220)
                    case .success(let image):
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fit)
                            .frame(height: 220)
                    case .failure:
                        VStack {
                            Text("Image unavailable")
                                .font(.body)
                                .foregroundColor(.secondary)
                        }
                        .frame(maxWidth: .infinity, minHeight: 220)
                        .background(Color(.systemGray5))
                    @unknown default:
                        EmptyView()
                    }
                }
            }
            .frame(maxWidth: .infinity)
            .background(Color(.systemGray5))
            .cornerRadius(12)
            
            // Content
            VStack(alignment: .leading, spacing: 4) {
                // Title and Favorite Button
                HStack(alignment: .center) {
                    Text(comic.title)
                        .font(.headline)
                        .fontWeight(.bold)
                        .lineLimit(1)
                    
                    Spacer()
                    
                    Button(action: onFavoriteToggle) {
                        Image(systemName: comic.isFavorite ? "heart.fill" : "heart")
                            .foregroundColor(comic.isFavorite ? .red : .secondary)
                            .font(.system(size: 20))
                    }
                    .frame(width: 40, height: 40)
                }
                
                // Comic Number and Date
                Text("Comic #\(comic.num) • \(comic.month)/\(comic.day)/\(comic.year)")
                    .font(.caption)
                    .foregroundColor(.secondary)
                
                // Alt Text
                if !comic.alt.isEmpty {
                    Text(comic.alt)
                        .font(.caption)
                        .foregroundColor(.secondary)
                        .lineLimit(2)
                }
            }
            .padding(12)
        }
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.1), radius: 4, x: 0, y: 2)
    }
}


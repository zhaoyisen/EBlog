export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: { code: string; message: string };
}

export interface LikeStatus {
  count: number;
  liked: boolean;
}

export interface LikeResponse {
  likeCount: number;
}

export interface FavoriteStatus {
  favorited: boolean;
}

export interface Comment {
  id: number;
  postId: number;
  authorId: number;
  content: string;
  createdAt: string;
}

export interface CreateCommentResponse {
  commentId: number;
}

function apiUrl(path: string): string {
  return `/api/v1${path}`;
}

export async function fetchLikeStatus(
  fetcher: typeof fetch,
  postId: number
): Promise<ApiResponse<LikeStatus>> {
  const res = await fetcher(apiUrl(`/posts/${postId}/likes`), {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' },
  });
  return await res.json();
}

export async function likePost(
  fetcher: typeof fetch,
  postId: number
): Promise<ApiResponse<LikeResponse>> {
  const res = await fetcher(apiUrl(`/posts/${postId}/likes`), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
  });
  return await res.json();
}

export async function unlikePost(
  fetcher: typeof fetch,
  postId: number
): Promise<ApiResponse<null>> {
  const res = await fetcher(apiUrl(`/posts/${postId}/likes`), {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json' },
  });
  return await res.json();
}

export async function fetchFavoriteStatus(
  fetcher: typeof fetch,
  postId: number
): Promise<ApiResponse<FavoriteStatus>> {
  const res = await fetcher(apiUrl(`/posts/${postId}/favorites`), {
    method: 'GET',
    headers: { 'Content-Type': 'application/json' },
  });
  return await res.json();
}

export async function favoritePost(
  fetcher: typeof fetch,
  postId: number
): Promise<ApiResponse<null>> {
  const res = await fetcher(apiUrl(`/posts/${postId}/favorites`), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
  });
  return await res.json();
}

export async function unfavoritePost(
  fetcher: typeof fetch,
  postId: number
): Promise<ApiResponse<null>> {
  const res = await fetcher(apiUrl(`/posts/${postId}/favorites`), {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json' },
  });
  return await res.json();
}

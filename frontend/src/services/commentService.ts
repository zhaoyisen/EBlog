import type { ApiResponse, Comment, CreateCommentResponse } from './interactionService';

function apiUrl(path: string): string {
  return `/api/v1${path}`;
}

export async function getComments(
  fetcher: typeof fetch,
  postId: number,
  limit: number = 20,
  offset: number = 0
): Promise<ApiResponse<Comment[]>> {
  const res = await fetcher(
    apiUrl(`/posts/${postId}/comments?limit=${limit}&offset=${offset}`),
    {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' },
    }
  );
  return await res.json();
}

export async function postComment(
  fetcher: typeof fetch,
  postId: number,
  content: string
): Promise<ApiResponse<CreateCommentResponse>> {
  const res = await fetcher(apiUrl(`/posts/${postId}/comments`), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ content }),
  });
  return await res.json();
}

export async function deleteComment(
  fetcher: typeof fetch,
  postId: number,
  commentId: number
): Promise<ApiResponse<null>> {
  const res = await fetcher(apiUrl(`/posts/${postId}/comments/${commentId}`), {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json' },
  });
  return await res.json();
}

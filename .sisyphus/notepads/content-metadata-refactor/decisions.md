## Public Data APIs Implementation

- **Metadata Sync Strategy**: Implemented `MetadataSyncRunner` to rebuild category and tag counts on startup.
    - Reason: Ensures consistency between `posts` table (source of truth) and `tags`/`categories` tables (read models).
    - Approach: In-memory aggregation of all PUBLIC/APPROVED posts, then Upsert to DB. Zero-out counts for tags/categories that no longer have posts.
- **TagController Optimization**: Switched `listTags` and `listCategories` to query `tags` and `categories` tables directly (O(1) vs O(N)).
- **UserController Activity**: Implemented `selectActiveUsers` in `UserMapper` using a LEFT JOIN and GROUP BY to sort users by post count.
    - Trade-off: Calculated on read. For high scale, this should be a column on `users` table updated via events, but for now this suffices (MVP).

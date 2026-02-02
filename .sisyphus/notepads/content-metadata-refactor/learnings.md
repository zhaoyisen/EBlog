## MyBatis Plus
- `UserMapper` needed manual `@Select` for aggregation query as MyBatis-Plus `QueryWrapper` doesn't easily support complex joins + aggregation + custom sorting without custom SQL or XML.
- `MetadataSyncRunner` implemented as `CommandLineRunner` to ensure tags/categories are populated on startup.
- Added simple `slugify` to `TagParser` for slug generation, replacing whitespace with dashes.

## Data Sync
- Task 1 (Database Schema): Completed
- Task 2 (Public Data APIs): Completed
- Task 3 (Admin Management APIs): Completed
- Task 4 (Sidebar Real Data): Completed
- Task 5 (Post Editor Upgrade): Completed

## Build & Verification
- Frontend build: Passed (after fixing syntax errors)
- Route generation: Successful
- Components: CategorySelect created

## Next Steps
- Task 6: Admin Management Pages (create admin/categories/page.tsx, admin/tags/page.tsx)

# CHANGELOG

## [1.0.0] — 2025-06-07

### Added
- Initial KMP project for marathon ticket booking (抢票)
- Data models: User, MarathonEvent, Order (with PENDING/PAID/CANCELLED/REFUNDED)
- EventRepository with mock data (3 sample events: BJ, SH, Xiamen)
- OrderRepository for order management
- MarathonHttpClient with Ktor (timeout, retry, content encoding)
- HomeScreen with hero banner, tab filter (报名中/即将开始/已结束), pull-to-refresh
- EventCard with progress bar, sold-out overlay, status badge
- DetailScreen with venue info, registration progress, grab button
- GrabDialog with countdown timer, grab animation, success/failure states
- OrdersScreen with order cards, cancel dialog
- CI/CD pipeline for Android and iOS

### Fixed
- APP.md corrected from generic "LaGe" template to describe marathon-ticket-kmp

### Security
- INTERNET + ACCESS_NETWORK_STATE declared
- HTTPS API endpoint configured (api.marathon.example.com)
- Token authentication placeholder (setToken available)

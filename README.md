# Lost & Found Portal

A REST API for reporting, browsing, and automatically matching lost and found items — built to be shippable in about a week.

## Why this project

- Simple, well-understood data model — mostly CRUD.
- Easy to demo end-to-end with Postman alone.
- One genuinely useful "smart" feature: automatic matching between lost and found reports, without needing any ML.

## Innovation: automatic matching

Instead of leaving users to manually search through found items, every new report is scored against open items on the opposite side using a simple, explainable rule engine:

| Signal | Points |
|---|---|
| Category match (exact) | 40 |
| Color match (exact or synonym) | 20 |
| Location match (same area / within distance threshold) | 20 |
| Date proximity (found date within N days of lost date) | 20 |

Pairs scoring above a threshold (e.g. 60) are inserted into `Matches` with `status = pending`, and a notification is written for both users. This runs automatically whenever a new lost or found item is created.

## Architecture

```
Client (Postman / UI)
        |
        v
   API layer (routes, validation)
        |
        v
Service layer (business logic + matching engine)
        |
        v
     Database
```

The matching engine, on finding a qualifying match, also writes to a `Notifications` table — so no live push mechanism is needed; clients simply poll `GET /notifications`.

## Database schema

**Users**
| Column | Type |
|---|---|
| id | uuid (PK) |
| name | string |
| email | string |
| phone | string |

**Lost_Items**
| Column | Type |
|---|---|
| id | uuid (PK) |
| user_id | uuid (FK -> Users) |
| category | string |
| color | string |
| location | string |
| date_lost | date |
| status | string (open / matched / claimed) |

**Found_Items**
| Column | Type |
|---|---|
| id | uuid (PK) |
| user_id | uuid (FK -> Users) |
| category | string |
| color | string |
| location | string |
| date_found | date |
| status | string (open / matched / claimed) |

**Matches**
| Column | Type |
|---|---|
| id | uuid (PK) |
| lost_item_id | uuid (FK -> Lost_Items) |
| found_item_id | uuid (FK -> Found_Items) |
| score | float |
| status | string (pending / confirmed / rejected) |

**Notifications**
| Column | Type |
|---|---|
| id | uuid (PK) |
| user_id | uuid (FK -> Users) |
| match_id | uuid (FK -> Matches) |
| is_read | boolean |
| created_at | timestamp |

## API reference

```
POST   /users                register a user
POST   /lost                  report a lost item -> triggers match scan
POST   /found                 report a found item -> triggers match scan
GET    /lost                  list/search lost items
GET    /found                 list/search found items
GET    /matches               list matches (optionally filter by user)
GET    /matches/{id}          match detail
PUT    /matches/{id}/confirm  mark a match as confirmed
PUT    /claim/{id}            claim an item, closes it out
GET    /notifications         list a user's notifications
PUT    /notifications/{id}    mark as read
DELETE /lost/{id}
DELETE /found/{id}
```

 
## Tech stack

Framework, ORM, and database are intentionally left open — this design works equally well with Node/Express, Django, or Spring Boot paired with PostgreSQL, MySQL, or MongoDB.

## Getting started

1. Clone the repository.
2. Configure your database connection (see your framework's `.env` / settings file).
3. Run migrations to create the tables described above.
4. Seed a few sample users, lost items, and found items to exercise the matching logic.
5. Import the Postman collection (once created) to test all endpoints.

## License

This project is licensed under the MIT License — see [LICENSE](LICENSE) for details.


# Pay My Buddy

An application that would allow customers to transfer money to manage their finances or pay their friends.


## Physical Data Model (PDM)

This document provides an overview of the physical data model used in the PayMyBuddy application. The model defines the structure of its database, the relationships between tables, and their attributes.

### Database Tables

#### 1. **`users` Table**
The `users` table represents the users of the PayMyBuddy application.

| Column           | Type            | Constraints                 |
|------------------|-----------------|-----------------------------|
| `id`             | `INTEGER`       | Primary key, Auto-increment |
| `username`       | `VARCHAR(255)`  | Unique                      |
| `email`          | `VARCHAR(255)`  | Unique                      |
| `password`       | `VARCHAR(255)`  |                             |

#### 2. **`transactions` Table**
The `transactions` table captures information about transactions between users.

| Column           | Type            | Constraints                           |
|------------------|-----------------|---------------------------------------|
| `id`             | `INTEGER`       | Primary key, Auto-increment           |
| `sender_id`      | `INTEGER`       | Foreign key referencing `users(id)`   |
| `receiver_id`    | `INTEGER`       | Foreign key referencing `users(id)`   |
| `description`    | `VARCHAR(255)`  |                                       |
| `amount`         | `FLOAT(53)`     | Not null                              |

#### 3. **`user_connections` Table**
The `user_connections` table specifies the connections (or friends) between users.

| Column           | Type            | Constraints                           |
|------------------|-----------------|---------------------------------------|
| `user_id`        | `INTEGER`       | Foreign key referencing `users(id)`   |
| `connection_id`  | `INTEGER`       | Foreign key referencing `users(id)`   |

### Relationships

#### 1. **Transactions**
Each transaction is associated with a sender and receiver, both of whom are users. This is represented by:
   - `sender_id`: Foreign key mapping to the `id` of the sender in the `users` table.
   - `receiver_id`: Foreign key mapping to the `id` of the receiver in the `users` table.

#### 2. **Connections**
The `user_connections` table defines many-to-many relationships between users. For example:
   - `user_id` represents a user.
   - `connection_id` represents another user who is connected to the `user_id`.

### Summary

The database model consists of three main entities:
- `users`: Stores user information.
- `transactions`: Defines transactions between users.
- `user_connections`: Maintains many-to-many relationships between users (connections/friends).

The relationships and constraints ensure data integrity, referential integrity, and support the application's functionality effectively.
## API Reference

### Users

#### Get all

```http
  GET /users
```

#### Get

```http
  GET /users?userId={id}
```

| Parameter | Type  | Description                      |
|-----------|-------|----------------------------------|
| `id`      | `int` | **Required** Id of user to fetch |

#### Get by email

```http
  GET /users/email?email={email}
```

| Parameter | Type     | Description                         |
|-----------|----------|-------------------------------------|
| `email`   | `string` | **Required** Email of user to fetch |

#### Register

```http
  POST /users
```

| Parameter  | Type     | Description                               |
|------------|----------|-------------------------------------------|
| `username` | `string` | **Required** The username of the new user |
| `email`    | `string` | **Required** The email of the new user    |
| `password` | `string` | **Required** The password of the new user |

#### Update

```http
  PUT /users
```

| Parameter  | Type     | Description                       |
|------------|----------|-----------------------------------|
| `id`       | `int`    | **Required** Id of user to update |
| `username` | `string` | The new username of the user      |
| `email`    | `string` | The new email of the user         |

#### Update password

```http
  PUT /users/password
```

| Parameter     | Type     | Description                               |
|---------------|----------|-------------------------------------------|
| `id`          | `int`    | **Required** Id of user to update         |
| `oldPassword` | `string` | **Required** The old password of the user |
| `password`    | `string` | **Required** The new password of the user |

#### Delete

```http
  DELETE /users?userId={id}
```

| Parameter | Type  | Description                       |
|-----------|-------|-----------------------------------|
| `id`      | `int` | **Required** Id of user to delete |

#### Login

```http
  POST /users/login
```

| Parameter  | Type     | Description                           |
|------------|----------|---------------------------------------|
| `email`    | `string` | **Required** The email of the user    |
| `password` | `string` | **Required** The password of the user |

#### Get connections

```http
  GET /users/{id}/connections
```

| Parameter | Type  | Description                                            |
|-----------|-------|--------------------------------------------------------|
| `id`      | `int` | **Required** Id of user to get all of it's connections |

#### Add connections

```http
  POST /users/{id}/connections
```

| Parameter      | Type  | Description                                            |
|----------------|-------|--------------------------------------------------------|
| `id`           | `int` | **Required** Id of user to get all of it's connections |
| `connectionId` | `int` | **Required** Id of the user to add connections         |

#### Delete connections

```http
  DELETE /users/{id}/connections
```

| Parameter      | Type  | Description                                            |
|----------------|-------|--------------------------------------------------------|
| `id`           | `int` | **Required** Id of user to get all of it's connections |
| `connectionId` | `int` | **Required** Id of the user to remove connections      |


### Transactions

#### Get

```http
  GET /transactions/id?transactionId={id}
```

| Parameter | Type  | Description                              |
|-----------|-------|------------------------------------------|
| `id`      | `int` | **Required** Id of transactions to fetch |

#### Get user transactions

```http
  GET /transactions/user?userId={id}
```

| Parameter | Type  | Description                                 |
|-----------|-------|---------------------------------------------|
| `id`      | `int` | **Required** Id of user to get transactions |

#### Get transactions between users

```http
  GET /transactions?senderId={senderId}&receiverId={receiverId}
```

| Parameter    | Type  | Description                 |
|--------------|-------|-----------------------------|
| `senderId`   | `int` | **Required** Id of sender   |
| `receiverId` | `int` | **Required** Id of receiver |

#### Add transactions

```http
  POST /transactions
```

| Parameter     | Type     | Description                             |
|---------------|----------|-----------------------------------------|
| `senderId`    | `int`    | **Required** Id of sender               |
| `receiverId`  | `int`    | **Required** Id of receiver             |
| `amount`      | `double` | **Required** Amount for the transaction |
| `description` | `string` | A description for the transaction       |


## Author

- [@Nikkune](https://www.github.com/Nikkune)


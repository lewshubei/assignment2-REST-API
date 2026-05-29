# Restaurant Ordering System

A Spring Boot web application for managing restaurant menu items. The system lets an admin add, edit, delete, search, sort, and filter menu items by food category. It also supports image uploads with saved image paths and local preview before submission.

## Features

- Menu dashboard with menu cards and uploaded food images
- Add and edit menu items
- Delete menu items
- Search by menu name, category, or description
- Filter by food category from existing menu data
- Sort by name or price
- Availability status display for available and unavailable items
- MySQL database integration using Spring Data JPA
- Thymeleaf templates for server-rendered pages

## Tech Stack

- Java 21
- Spring Boot 4.0.6
- Spring MVC
- Spring Data JPA
- Thymeleaf
- MySQL
- Maven Wrapper

## Project Structure

```text
assignment2-REST-API/
|-- README.md
`-- restaurant-ordering-system/
    |-- pom.xml
    |-- restaurant_db.sql
    |-- uploads/
    `-- src/
        |-- main/java/com/restaurant/
        `-- main/resources/
            |-- application.properties
            |-- static/css/app.css
            `-- templates/
```

## Setup Steps

1. Install the required software:

```text
Java 21
MySQL or MariaDB
```

2. Create and import the database.

Create a database named `restaurant_db`, then import:

```text
restaurant-ordering-system/restaurant_db.sql
```

3. Check database settings.

Open:

```text
restaurant-ordering-system/src/main/resources/application.properties
```

Default configuration:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/restaurant_db
spring.datasource.username=root
spring.datasource.password=
```

Update the username or password if your local MySQL setup is different.

4. Run the application.

From the project folder:

```bash
cd restaurant-ordering-system
./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
cd restaurant-ordering-system
.\mvnw.cmd spring-boot:run
```

5. Open the app in a browser.

```text
http://localhost:8080
```

Main pages:

```text
/menu      Menu dashboard
/addMenu   Add new menu item
```

## Usage Instructions

After opening the application, use the menu dashboard to manage the restaurant catalog:

1. Click `Add Item` or `Add Menu Item` to create a new menu record.
2. Fill in the menu name, category, price, availability, description, and image.
3. Select an image to preview it before saving.
4. Click `Save Menu` to store the item in the database.
5. Use `Edit` on a menu card to update an existing item.
6. Use `Delete` on a menu card to remove an item.
7. Use the search box to find items by name, category, or description.
8. Use the category filter chips or category dropdown to filter menu items.
9. Use the sort dropdown to order items by name or price.

## Postman API Testing

The project also exposes a REST API for menu items. Use these endpoints in Postman to test the backend in JSON format:

| Method | Endpoint                                    | Purpose                      |
| ------ | ------------------------------------------- | ---------------------------- |
| GET    | `http://localhost:8080/api/menu-items`      | Get all menu items           |
| GET    | `http://localhost:8080/api/menu-items/{id}` | Get one menu item by ID      |
| POST   | `http://localhost:8080/api/menu-items`      | Create a new menu item       |
| PUT    | `http://localhost:8080/api/menu-items/{id}` | Update an existing menu item |
| DELETE | `http://localhost:8080/api/menu-items/{id}` | Delete a menu item           |

### Postman Setup

1. Start the Spring Boot application first.
2. Open Postman and create a new request.
3. Select the correct HTTP method.
4. Enter the URL, for example `http://localhost:8080/api/menu-items`.
5. For `POST` and `PUT`, set the header `Content-Type: application/json`.
6. Choose the `Body` tab, select `raw`, and choose `JSON`.
7. Send the request and check the JSON response.

### Sample JSON Request

Use this body for `POST`:

```json
{
  "name": "Chicken Burger",
  "description": "Grilled chicken burger with fries",
  "category": "Western Food",
  "price": 12.5,
  "availability": "Available",
  "imagePath": "/uploads/chicken-burger.jpg"
}
```

### Sample JSON Response

Example response from `GET /api/menu-items` or `POST /api/menu-items`:

```json
{
  "id": 1,
  "name": "Chicken Burger",
  "description": "Grilled chicken burger with fries",
  "category": "Western Food",
  "price": 12.5,
  "availability": "Available",
  "imagePath": "/uploads/chicken-burger.jpg"
}
```

If a required field is missing, the API returns a JSON error message with status `400 Bad Request`.

## Database Schema

The application uses one main table named `menu_items`.

| Column         | Type    | Purpose                                                         |
| -------------- | ------- | --------------------------------------------------------------- |
| `id`           | bigint  | Primary key for each menu item                                  |
| `name`         | varchar | Menu item name                                                  |
| `description`  | text    | Menu item description                                           |
| `category`     | varchar | Food category such as Malay Food, Chinese Food, or Western Food |
| `price`        | double  | Menu item price in RM                                           |
| `availability` | varchar | Availability status, usually Available or Unavailable           |
| `image_path`   | varchar | Relative path to the uploaded menu image                        |

The provided `restaurant_db.sql` file includes sample records so the dashboard has data after import.

## Image Uploads

Uploaded menu images are stored in:

```text
restaurant-ordering-system/uploads/
```

The upload directory is configured in `application.properties`:

```properties
app.upload-dir=uploads
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

## Troubleshooting

If the application cannot connect to MySQL:

- Make sure MySQL or MariaDB is running.
- Confirm that the `restaurant_db` database exists.
- Check the username and password in `application.properties`.
- Make sure the database is available at `localhost:3306`.

If uploaded images do not appear:

- Confirm the `uploads/` folder exists inside `restaurant-ordering-system`.
- Check that `app.upload-dir=uploads` is still set in `application.properties`.
- Restart the Spring Boot application after changing upload configuration.

If port `8080` is already in use:

- Stop the other application using port `8080`, or configure a different server port in `application.properties`.

## Run Tests

From `restaurant-ordering-system`:

```bash
./mvnw test
```

On Windows PowerShell:

```powershell
.\mvnw.cmd test
```

## Code Organisation

The project follows a simple Spring MVC structure:

- `controller/` handles web requests and page navigation.
- `DBService/` contains service-layer logic for menu operations.
- `repository/` contains Spring Data JPA database access.
- `entity/` contains the `MenuItem` database entity.
- `templates/` contains Thymeleaf HTML pages.
- `static/css/` contains shared styling.
- `uploads/` stores uploaded menu images.

The code includes comments for the main controller actions and keeps business logic separated from the templates where possible.

## Notes for Reviewers

This repository is designed to be easy to run locally:

- It includes Maven Wrapper files, so Maven does not need to be installed separately.
- It includes a database SQL file for quick setup.
- It uses relative upload paths so menu images can be served locally.
- It documents setup, usage, database structure, testing, and troubleshooting in this README.

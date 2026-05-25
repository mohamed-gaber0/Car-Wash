# Car Wash Management System
## Overview
The Car Wash Management System is a comprehensive, desktop-based application designed to streamline the operations of a car wash business. It handles various aspects including customer management, service tracking, product sales, billing, and reporting. The system uses a local database for reliable data storage and provides a user-friendly graphical interface.
## Features
- **Dashboard:** A central hub providing an overview of daily operations and quick access to key functions.
- **Customer Management:** Register and manage customer profiles.
- **Service & Product Management:** Track car wash services provided and products sold.
- **Billing & Payments:** Process customer payments, generate receipts, and support barcode scanning.
- **Reporting:** Generate and view detailed reports on sales, services, and employee performance.
- **User Management:** Secure access with different roles (e.g., Admin, Cashier).
## Demo Video
[![Watch the Demo] (https://www.youtube.com/watch?v=bM63a0n5pAc)
*(Replace `YOUR_VIDEO_ID` with the actual video ID, or replace this section with your uploaded video link)*
## Technologies Used
- **Language:** Java
- **UI Framework:** Java Swing / JavaFX (depending on implementation)
- **Database:** SQLite (`carwash.db`)
- **Build Tool:** Maven
## Setup and Installation
### Prerequisites
- Java Development Kit (JDK) 11 or higher installed.
- Maven installed.
### Instructions
1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/your-repo-name.git
   cd your-repo-name
   ```
2. **Build the project using Maven:**
   ```bash
   mvn clean install
   ```
3. **Run the application:**
   ```bash
   mvn exec:java -Dexec.mainClass="oop.carwash.ui.LoginScreen"
   ```
   *(Note: Adjust the main class if the entry point is different).*
## Database
The application relies on a local SQLite database named `carwash.db`. Ensure this file is present in the root directory or the application's working directory.
## Contributing
Contributions are welcome! Please fork the repository and submit a pull request with your enhancements.
## License
This project is licensed under the MIT License - see the LICENSE file for details.

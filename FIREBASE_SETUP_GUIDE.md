# Firebase Setup & Deployment Guide for College Canteen Food Ordering App

This comprehensive guide will help you connect your Android app (`com.canteen.foodordering`) to your own Firebase project, configure Cloud Firestore, Firebase Authentication, and Firebase Storage, and run the app successfully.

---

## Step 1: Create a Firebase Project

1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Click **Create a project** (or **Add project**).
3. Enter your project name (e.g., `College Canteen App`) and click **Continue**.
4. (Optional) Disable Google Analytics for simplicity, or enable it if desired.
5. Click **Create project** and wait for the initialization to complete.

---

## Step 2: Register Android App & Add `google-services.json`

1. In your Firebase Console project overview, click the **Android icon** (`+ Add app` -> `Android`).
2. Enter the exact Package Name:
   ```
   com.canteen.foodordering
   ```
3. Enter an App nickname (e.g., `Canteen App`).
4. Click **Register app**.
5. Download `google-services.json`.
6. Open your project folder `CANTEEN ORDER APP/app/` in Android Studio or File Explorer and **replace** the placeholder `app/google-services.json` with the file you just downloaded.

---

## Step 3: Enable Firebase Authentication (Email/Password)

1. In the left sidebar of the Firebase Console, navigate to **Build** -> **Authentication**.
2. Click **Get Started**.
3. Under the **Sign-in method** tab, click **Email/Password**.
4. Enable the **Email/Password** toggle (leave Email link disabled).
5. Click **Save**.

---

## Step 4: Enable Cloud Firestore Database

1. In the Firebase Console left sidebar, go to **Build** -> **Firestore Database**.
2. Click **Create database**.
3. Choose your database location (e.g., `asia-south1` or closest to your users) and click **Next**.
4. Select **Start in test mode** for initial testing (or production mode with security rules).
5. Click **Create**.

### Security Rules (Firestore)
Go to the **Rules** tab in Cloud Firestore and update them as follows:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Users collection: Authenticated users can read/write their own document
    match /users/{userId} {
      allow read, write: if request.auth != null;
    }
    
    // Food Items collection: Anyone authenticated can read; Admin can create/update/delete
    match /food_items/{itemId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
    
    // Orders collection: Authenticated users can place & read orders; Admin can update status
    match /orders/{orderId} {
      allow read, write: if request.auth != null;
    }
  }
}
```
Click **Publish**.

---

## Step 5: Enable Firebase Storage (For Food Images)

1. In the Firebase Console left sidebar, go to **Build** -> **Storage**.
2. Click **Get Started**.
3. Select **Start in test mode** and click **Next**.
4. Choose the default storage bucket location and click **Done**.

### Security Rules (Storage)
Go to the **Rules** tab in Firebase Storage and update them:

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /food_images/{allPaths=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```
Click **Publish**.

---

## Step 6: Database Collections & Schema Reference

### 1. Collection: `users`
- **Document ID**: User UID (`FirebaseAuth.getInstance().getCurrentUser().getUid()`)
- **Fields**:
  - `uid` (string)
  - `name` (string)
  - `email` (string)
  - `phone` (string)
  - `role` (string: `"student"` or `"admin"`)

### 2. Collection: `food_items`
- **Document ID**: Auto-generated string
- **Fields**:
  - `id` (string)
  - `name` (string)
  - `description` (string)
  - `price` (number/double)
  - `category` (string: `"Breakfast"`, `"Meals"`, `"Snacks"`, `"Beverages"`, `"Desserts"`)
  - `imageUrl` (string: Firebase Storage HTTP URL)
  - `available` (boolean)

### 3. Collection: `orders`
- **Document ID**: Auto-generated string
- **Fields**:
  - `orderId` (string)
  - `studentId` (string)
  - `studentName` (string)
  - `studentPhone` (string)
  - `items` (array of maps):
    - `foodId`, `foodName`, `price`, `quantity`, `imageUrl`
  - `totalPrice` (number/double)
  - `status` (string: `"Placed"`, `"Preparing"`, `"Ready"`, `"Delivered"`)
  - `timestamp` (number/long)
  - `notes` (string)

---

## Step 7: How to Run & Test in Android Studio

1. Open **Android Studio** (Giraffe, Hedgehog, Iguana or later).
2. Select **Open an existing project** and choose the folder:
   `c:\Users\nikhi\OneDrive\Desktop\CANTEEN ORDER APP`
3. Wait for Gradle Sync to complete.
4. Connect an **Android Emulator** (API 24+) or a physical Android phone via USB Debugging.
5. Click **Run 'app'** (`Shift + F10`).

### Testing the Admin Workflow:
1. Launch the app and tap **Sign Up**.
2. Select **Admin Staff**, fill in details, and complete registration.
3. On the **Manage Menu** tab, click **+** (FAB).
4. Fill in food title, description, price, pick category, select an image from gallery, and click **Save Item**.
5. The food item will be uploaded to Firebase Storage and immediately listed in the food catalog!

### Testing the Student Workflow:
1. Install/Run on a second emulator or log out from Admin.
2. Sign up a new account with role **Student**.
3. Browse food items, filter by categories (Snacks, Beverages, etc.), or search in real time.
4. Tap **Add to Cart**, then tap the FAB Cart icon to open the cart.
5. Adjust quantities, enter table notes, and tap **Place Order**.
6. Switch back to the **Admin** account under **All Orders** tab to view the live order and change its status from `Placed` -> `Preparing` -> `Ready` -> `Delivered`!

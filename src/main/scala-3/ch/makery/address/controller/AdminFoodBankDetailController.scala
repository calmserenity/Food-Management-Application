package ch.makery.address.controller

import ch.makery.address.MainApp
import ch.makery.address.model.FoodBank
import javafx.fxml.FXML
import javafx.scene.control.{Button, Label}
import javafx.scene.image.{Image, ImageView}

class AdminFoodBankDetailController {
  
  // FXML UI element injections
  @FXML private var foodBankNameTitle: Label = _
  @FXML private var foodBankImageView: ImageView = _
  @FXML private var locationLabel: Label = _
  @FXML private var hoursLabel: Label = _
  @FXML private var picLabel: Label = _
  @FXML private var phoneLabel: Label = _
  @FXML private var emailLabel: Label = _
  @FXML private var needsLabel: Label = _
  @FXML private var volunteerScheduleButton: Button = _
  @FXML private var inventoryButton: Button = _
  @FXML private var editButton: Button = _

  // Holds the food bank currently being displayed
  private var currentFoodBank: Option[FoodBank] = None

  // Initializes the controller with data for a specific food bank
  def initData(foodBank: FoodBank): Unit = {
    currentFoodBank = Some(foodBank)

    foodBankNameTitle.setText(foodBank.name)
    locationLabel.setText(foodBank.location)
    picLabel.setText(foodBank.personInCharge)

    hoursLabel.setText(foodBank.operatingHours.getOrElse("Not specified"))
    phoneLabel.setText(foodBank.phoneNumber.getOrElse("Not specified"))
    emailLabel.setText(foodBank.email.getOrElse("Not specified"))
    needsLabel.setText(foodBank.currentNeeds.getOrElse("No specific needs listed at this time."))

    foodBank.imagePath.orElse(Some("/image/foodbankpic1.png")).foreach { path =>
      try {
        val imageStream = getClass.getResourceAsStream(path)
        if (imageStream == null) throw new NullPointerException(s"Image resource not found at path: $path")
        foodBankImageView.setImage(new Image(imageStream))
      } catch {
        case e: Exception => println(s"Could not load image: $path")
      }
    }
  }

  // Handles the click event for the back button
  @FXML
  private def handleBackClick(): Unit = {
    MainApp.navigateBack()
  }
  
  // Handles the click event for the edit button
  @FXML
  private def handleEdit(): Unit = {
    currentFoodBank.foreach { fb =>
      println(s"Edit button clicked for ${fb.name}")
      MainApp.showEditFoodBankView(fb)
    }
  }

  // Handles the click event for the active requests button
  @FXML
  private def handleActiveRequests(): Unit = {
    currentFoodBank.foreach(MainApp.showActiveRequestsView)
  }

  // Handles the click event for the volunteer schedule button
  @FXML
  private def handleVolunteerSchedule(): Unit = {
    currentFoodBank.foreach(MainApp.showVolunteerManagementView)
  }

  // Handles the click event for the inventory button
  @FXML
  private def handleInventory(): Unit = {
    currentFoodBank.foreach(MainApp.showInventoryManagementView)
  }
}
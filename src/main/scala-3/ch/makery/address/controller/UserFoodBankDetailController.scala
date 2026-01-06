package ch.makery.address.controller

import ch.makery.address.MainApp
import ch.makery.address.model.{FoodBank, FoodItem}
import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.scene.control._
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.{ColumnConstraints, GridPane, VBox}
import scalafx.Includes._
import javafx.collections.FXCollections
import javafx.scene.control.cell.PropertyValueFactory

class UserFoodBankDetailController {
  
  // FXML UI element injections
  @FXML private var foodBankNameTitle: Label = _
  @FXML private var foodBankImageView: ImageView = _
  @FXML private var locationLabel: Label = _
  @FXML private var hoursLabel: Label = _
  @FXML private var picLabel: Label = _
  @FXML private var phoneLabel: Label = _
  @FXML private var emailLabel: Label = _
  @FXML private var needsLabel: Label = _
  @FXML private var volunteerButton: Button = _
  @FXML private var donateButton: Button = _
  @FXML private var requestButton: Button = _
  @FXML private var foodAccordion: Accordion = _

  // Holds the food bank currently being displayed
  private var currentFoodBank: Option[FoodBank] = None

  // Initializes the controller with data for a specific food bank
  def initData(foodBank: FoodBank): Unit = {
    currentFoodBank = Some(foodBank)

    // Populate UI elements with food bank data
    foodBankNameTitle.setText(foodBank.name)
    locationLabel.setText(foodBank.location)
    picLabel.setText(foodBank.personInCharge)
    hoursLabel.setText(foodBank.operatingHours.getOrElse("Not specified"))
    phoneLabel.setText(foodBank.phoneNumber.getOrElse("Not specified"))
    emailLabel.setText(foodBank.email.getOrElse("Not specified"))
    needsLabel.setText(foodBank.currentNeeds.getOrElse("No specific needs listed at this time."))

    // Load the food bank's image, with a fallback to a default image
    foodBank.imagePath.orElse(Some("/image/foodbanklistbox.png")).foreach { path =>
      try {
        val imageStream = getClass.getResourceAsStream(path)
        if (imageStream == null) {
          // Fallback to default if resource is not found
          foodBankImageView.setImage(new Image(getClass.getResourceAsStream("/image/foodbankpic1.png")))
        } else {
          foodBankImageView.setImage(new Image(imageStream))
        }
      } catch {
        case e: Exception =>
          println(s"Could not load image: $path. Error: ${e.getMessage}")
          foodBankImageView.setImage(new Image(getClass.getResourceAsStream("/image/defaultfoodbank.png")))
      }
    }
    foodBank.id.foreach(loadFoodItems)
  }

  // Fetches and displays the food items available at the current food bank
  private def loadFoodItems(foodBankId: Long): Unit = {
    foodAccordion.getPanes.clear()

    val items = FoodItem.findByFoodBank(foodBankId)

    if (items.isEmpty) {
      val emptyLabel = new Label("No food items currently listed.")
      val emptyPane = new TitledPane("Available Food", emptyLabel)
      emptyPane.setCollapsible(false)
      foodAccordion.getPanes.add(emptyPane)
    } else {
      val itemsByCategory = items.groupBy(_.category)
      val sortedCategories = itemsByCategory.keys.toSeq.sorted

      // Create a table for each food category and add it to the accordion
      sortedCategories.foreach { category =>
        val table = new TableView[FoodItem]()
        val categoryItems = itemsByCategory(category).sortBy(_.name)
        table.setItems(FXCollections.observableArrayList(categoryItems: _*))
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN)

        val nameCol = new TableColumn[FoodItem, String]("Item")
        nameCol.setCellValueFactory(new PropertyValueFactory[FoodItem, String]("name"))

        val quantityCol = new TableColumn[FoodItem, Int]("Quantity")
        quantityCol.setCellValueFactory(new PropertyValueFactory[FoodItem, Int]("quantity"))
        quantityCol.setMaxWidth(150)
        quantityCol.setMinWidth(150)

        table.getColumns.setAll(nameCol, quantityCol)
        table.getStyleClass.add("food-item-table") 

        val titledPane = new TitledPane(category, table)
        titledPane.getStyleClass.add("food-category-pane")

        foodAccordion.getPanes.add(titledPane)
      }

      if (!foodAccordion.getPanes.isEmpty) {
        foodAccordion.setExpandedPane(foodAccordion.getPanes.get(0))
      }
    }
  }


  // Handles the click event for the back button
  @FXML
  private def handleBackClick(): Unit = {
    MainApp.navigateBack()
  }

  // Handles the click event for the volunteer button
  @FXML
  private def handleVolunteer(): Unit = {
    currentFoodBank.foreach(MainApp.showUserVolunteerView)
  }

  // Handles the click event for the request donation button
  @FXML
  private def handleRequest(): Unit = {
    currentFoodBank.foreach(MainApp.showRequestDonationView)
  }
  
  // Handles the click event for the donate button
  @FXML
  private def handleDonate(): Unit = {
    currentFoodBank.foreach(MainApp.showDonationFormView)
  }
}
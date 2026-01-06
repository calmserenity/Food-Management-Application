package ch.makery.address.controller

import ch.makery.address.MainApp
import ch.makery.address.model.FoodBank
import javafx.fxml.FXML
import javafx.scene.control.{Label, TextField}
import javafx.scene.layout.{GridPane, VBox}
import scalafx.Includes._
import scalafx.application.Platform

class UserFoodBankViewController {

  // FXML UI element injections
  @FXML private var foodBankListContainer: VBox = _
  @FXML private var searchField: TextField = _

  // Holds the full list of food banks to avoid repeated database calls
  private var allFoodBanks: List[FoodBank] = List()

  @FXML
  def initialize(): Unit = {
    // Add a listener to the search field to filter results dynamically
    searchField.text.onChange { (_, _, newValue) =>
      filterAndDisplayFoodBanks(newValue)
    }

    // Load food banks from the database after the UI has loaded
    Platform.runLater {
      loadAndStoreFoodBanks()
    }
  }

  // Fetches all food banks from the database and stores them in memory
  private def loadAndStoreFoodBanks(): Unit = {
    allFoodBanks = FoodBank.findAll()
    filterAndDisplayFoodBanks("")
  }

  // Filters the list of food banks based on the search text and updates the view
  private def filterAndDisplayFoodBanks(searchText: String): Unit = {
    val filteredList = if (searchText == null || searchText.isEmpty) {
      allFoodBanks
    } else {
      val lowerCaseSearchText = searchText.toLowerCase
      allFoodBanks.filter { foodBank =>
        foodBank.name.toLowerCase.contains(lowerCaseSearchText) ||
          foodBank.location.toLowerCase.contains(lowerCaseSearchText)
      }
    }

    // Clear the existing cards and display the filtered results
    foodBankListContainer.getChildren.clear()

    filteredList.foreach { foodBank =>
      val card = createFoodBankCard(foodBank)
      foodBankListContainer.getChildren.add(card)
    }
  }

  // Creates a UI card for a single food bank
  private def createFoodBankCard(foodBank: FoodBank): VBox = {
    val nameLabel = new Label(foodBank.name)
    nameLabel.getStyleClass.add("card-data-name")
    val locationLabel = new Label(foodBank.location)
    locationLabel.getStyleClass.add("card-data")

    val grid = new GridPane()
    grid.setHgap(10)
    grid.setVgap(8)
    grid.add(new Label("Food Bank Name:"), 0, 0)
    grid.add(nameLabel, 1, 0)
    grid.add(new Label("Location:"), 0, 1)
    grid.add(locationLabel, 1, 1)

    // Apply a specific style to the labels in the first column
    grid.getChildren.forEach { node =>
      if (GridPane.getColumnIndex(node) == 0) {
        node.getStyleClass.add("card-label")
      }
    }

    val cardVBox = new VBox(grid)
    cardVBox.getStyleClass.add("food-bank-card")

    // Add a click event to the card to show the detail view
    cardVBox.onMouseClicked = _ => {
      println(s"Card for ${foodBank.name} clicked by user!")
      MainApp.showUserFoodBankDetailView(foodBank)
    }

    cardVBox
  }

  // Handles the click event for the back button
  @FXML
  private def handleBackClick(): Unit = {
    MainApp.navigateBack()
  }
}
package ch.makery.address.controller

import ch.makery.address.MainApp
import ch.makery.address.model.FoodBank
import javafx.beans.value.{ChangeListener, ObservableValue}
import javafx.fxml.FXML
import javafx.scene.control.{Label, TextField}
import javafx.scene.layout.{GridPane, VBox}
import scalafx.Includes._
import scalafx.application.Platform

class AdminFoodBankController {

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

  // Handles the click event for the back button
  @FXML
  private def handleBackClick(): Unit = {
    MainApp.navigateBack()
  }

  // Fetches all food banks from the database and stores them in memory
  private def loadAndStoreFoodBanks(): Unit = {
    allFoodBanks = FoodBank.findAll()
    filterAndDisplayFoodBanks("")
  }

  // Handles the click event for the "Add Food Bank" button
  @FXML
  private def handleAddFoodBank(): Unit = {
    println("Add Food Bank button clicked!")
    MainApp.showAddFoodBankView()
  }

  // Filters the list of food banks based on the search text and updates the view
  private def filterAndDisplayFoodBanks(searchText: String): Unit = {
    val filteredList = if (searchText == null || searchText.isEmpty) {
      allFoodBanks
    } else {
      val lowerCaseSearchText = searchText.toLowerCase
      allFoodBanks.filter { foodBank =>
        foodBank.name.toLowerCase.contains(lowerCaseSearchText) ||
          foodBank.location.toLowerCase.contains(lowerCaseSearchText) ||
          foodBank.personInCharge.toLowerCase.contains(lowerCaseSearchText)
      }
    }

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
    val picLabel = new Label(foodBank.personInCharge)
    picLabel.getStyleClass.add("card-data")
    val yearLabel = new Label(foodBank.yearFounded.toString)
    yearLabel.getStyleClass.add("card-data")

    val grid = new GridPane()
    grid.setHgap(10)
    grid.setVgap(8)
    grid.add(new Label("Food Bank Name:"), 0, 0)
    grid.add(nameLabel, 1, 0)
    grid.add(new Label("Location:"), 0, 1)
    grid.add(locationLabel, 1, 1)
    grid.add(new Label("Person-in-Charge:"), 0, 2)
    grid.add(picLabel, 1, 2)
    grid.add(new Label("Year Founded:"), 0, 3)
    grid.add(yearLabel, 1, 3)

    grid.getChildren.forEach { node =>
      if (GridPane.getColumnIndex(node) == 0) {
        node.getStyleClass.add("card-label")
      }
    }

    val cardVBox = new VBox(grid)
    cardVBox.getStyleClass.add("food-bank-card")

    // Add a click event to the card to show the detail view
    cardVBox.onMouseClicked = _ => {
      println(s"Card for ${foodBank.name} clicked!")
      MainApp.showFoodBankDetailView(foodBank)
    }

    cardVBox
  }
}
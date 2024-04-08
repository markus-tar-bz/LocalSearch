package com.markus.localsearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class MainViewModel : ViewModel() {
    /*Here we define the states we need in this view model. An exposed version and a private version
    to make sure that the view model class can change the state but the ui cannot.*/

    private val _searchText = MutableStateFlow("") //empty string by default
    val searchText = _searchText.asStateFlow() //public exposed version of the search text
    /*Make sure that the viewmodel can change the mutable one but the ui should not be able to change
    so we expose the immutable version as a version of the unexposed view model state.*/

    private val _isSearching = MutableStateFlow(false) //state to show the prox bar or hide it
    val isSearching = _isSearching.asStateFlow()

    private val _persons = MutableStateFlow(allPersons)
    //Here we now apply our search logic
    //make persons dependant on the searchText state flow
    //called when either the search text or _persons state changes i.e. combine keyword
    //combine so that if state of _persons changes, e.g.when more persons are loaded into the api, it will be applied in our current search query
    val persons = searchText
        .combine(_persons) { text, persons -> //A reference to or searchText and current persons state.
        //So if we change our searchText we can map the results here
            if(text.isBlank()) {
                persons
            } else {
                persons.filter {
                    it.doesMatchSearchQuery(text)
                }
            }
        }
    //Make state flow keep the latest value/Caching. Makes sure our state stays updated with the ui state.
        .stateIn( //converts our normal state(persons) into state flow
            viewModelScope, //launch the flow in our view model scope
            SharingStarted.WhileSubscribed(5000), /*pass this so that if the subscriber or collector disappears the persons block will
            be executed for 5 more seconds. */
            _persons.value //initial value = listOf<Person>() = allPersons
        )

    //called from ui whenever the usr types sth
    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    //Our Business Logic
    data class Person(
        val firstName: String,
        val lastName: String
    ) {
        //A fun to define our search criteria or whether the data meets it.
        fun doesMatchSearchQuery(query: String): Boolean {
            val matchingCombinations = listOf(
                "$firstName$lastName",
                "$firstName $lastName",
                "{${firstName.first()}${lastName.first()}}",
                "{${firstName.first()} ${lastName.first()}}"
            )
            return matchingCombinations.any {
                it.contains(query, ignoreCase = true)
            //returns if there is any combination in our list of combinations that matches our query e.g. rk in Mark
            }
        }
    }
}

private val allPersons = listOf(
    MainViewModel.Person(
        firstName = "Mark",
        lastName = "Ndaru"
    ),
    MainViewModel.Person(
        firstName = "Darius",
        lastName = "Nyaga"
    ),
    MainViewModel.Person(
        firstName = "Anthony",
        lastName = "Mwalili"
    ),
    MainViewModel.Person(
        firstName = "Steve",
        lastName = "Magu"
    )
)
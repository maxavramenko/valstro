Import and build this project with maven

To run the environment
1. Start the backend Starwars websocket server using Docker
2. Launch the main Java client StarwarsMain
3. Enter the search criteria at the prompt. Multiple search terms could be entered using a comma
4. To shutdown the client enter "exit' at the prompt

Example:

Starwars API started at Wed May 24 12:54:15 EDT 2023

Usage:
Enter Starwars character names at the prompt
You can use partial name search
Use a comma to enter multiple names
Type 'exit' to quit the application


Enter Starwars character search criteria:
Test,Luke,Ana,Darth,Blah
No valid matches retrieved for query 'Test'
(1/1) Luke Skywalker - [A New Hope, The Empire Strikes Back, Return of the Jedi, Revenge of the Sith]
No valid matches retrieved for query 'Blah'
(1/2) Darth Vader - [A New Hope, The Empire Strikes Back, Return of the Jedi, Revenge of the Sith]
(2/2) Darth Maul - [The Phantom Menace]
(1/4) Leia Organa - [A New Hope, The Empire Strikes Back, Return of the Jedi, Revenge of the Sith]
(2/4) Anakin Skywalker - [The Phantom Menace, Attack of the Clones, Revenge of the Sith]
(3/4) Quarsh Panaka - [The Phantom Menace]
(4/4) Bail Prestor Organa - [Attack of the Clones, Revenge of the Sith]

Enter Starwars character search criteria:
exit
StarwarsAPI: Shutting down

Process finished with exit code 0
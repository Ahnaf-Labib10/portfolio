import random

choices = ("rock", "paper", "scissor")
running = True

while running:
    player_choice = input("Enter rock, paper, or scissor: ")
    computer_choice = random.choice(choices)
    
    if player_choice not in choices:
        print("Invalid choice.")
    else:
        print(f"player: {player_choice}")
        print(f"computer: {computer_choice}")

        if player_choice == computer_choice:
            print("It's a tie!")
        elif (player_choice == "rock" and computer_choice == "scissor") or \
            (player_choice == "paper" and computer_choice == "rock") or \
            (player_choice == "scissor" and computer_choice == "paper"):
            print("You win!")
        else:
            print("You lose!")
    while True:
     again = input("Play again? (yes/no): ").lower()
     if again == "y":
       break
     elif again == "n":
        running = False
        break
     else:
        print("Invalid input, choose again.")
print("Thanks for playing!")
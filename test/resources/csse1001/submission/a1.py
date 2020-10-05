"""
CSSE1001 Assignment 1
Semester 2, 2020
"""

from a1_support import *

# Fill these in with your details
__author__ = "{{user.name}} ({{user.id}})"
__email__ = ""
__date__ = ""


SPACE = " "
STAR = "*"
LINE = "-"
MIN_WORD_LENGTH = 6

# Write your code here (i.e. functions)


def select_word_at_random(word_select)-> str:
    """
    select word at random(word select)-> str:
    Given the word select is either \FIXED" or \ARBITRARY" this function will return a string randomly
    selected from WORDS FIXED.txt or WORDS ARBITRARY.txt respectively. If word select is anything
    other then the expected input then this function should return None.
    Hint: see a1 support.load words() and a1 support.random index()
    """
    if word_select != "FIXED" and word_select != "ARBITRARY":
        return None

    words = load_words(word_select)
    num = random_index(words)
    return words[num]



def create_guess_line(guess_no, word_length)-> str:
    """
    This function returns the string representing the display corresponding to the guess number integer, guess no.

    """

    num = word_length - MIN_WORD_LENGTH
    guess_range = GUESS_INDEX_TUPLE[num][guess_no - 1]
    lower_bound = guess_range[0]
    upper_bound = guess_range[1]

    output = f'Guess {guess_no}{WALL_VERTICAL}'

    i = 0
    while i < word_length:
        if lower_bound <= i <= upper_bound:
            output += SPACE + STAR
        else:
            output += SPACE + LINE

        output += SPACE + WALL_VERTICAL
        i += 1

    return output

def create_top_line(word_length)-> str:
    output = "       |"
    i = 1
    while i <= word_length:
        output += SPACE + str(i) + SPACE + WALL_VERTICAL
        i += 1
    return output

def create_line_separator(word_length)->str:
    num = 9 + 4 * word_length
    output = ""

    while num > 0:
        output += LINE
        num -= 1

    return output


def display_guess_matrix(guess_no, word_length, scores)-> None:
    """
    This function prints the progress of the game. This includes all line strings for guesses up to guess no with
    their corresponding scores (a tuple containing all previous scores), and the line string for guess no (without
    a score).
    """

    line = create_line_separator(word_length)
    print(create_top_line(word_length))
    print(line)
    i = 1
    while i <= guess_no:
        output = create_guess_line(i, word_length)
        if i != guess_no:
            output += f'   {scores[i - 1]} Points'
        print(output)
        print(line)
        i += 1



def compute_value_for_guess(word, start_index, end_index, guess)-> int:
    """
    Return the score, an integer, the player is awarded for a specific guess. The word is a string representing the
    word the player has to guess. The substring to be guessed is determined by the start index and end index.
    The substring is created by slicing the word from the start index up to and including the end index. The
    guess is a string representing the guess attempt the player has made.
    """

    sub_word = word[start_index : end_index + 1]
    score = 0
    for guess_char, word_char in zip(guess, sub_word):
        if guess_char is word_char:
            if guess_char in VOWELS:
                score += 14
                continue
            else:
                score += 12
                continue
        elif guess_char in sub_word:
            score += 5

    return score


def game():
    mode = input("Do you want a 'FIXED' or 'ARBITRARY' length word?: ")
    word = select_word_at_random(mode)
    print("Now try and guess the word, step by step!!")

    word_length = len(word)
    guess_no = 1
    scores = ()

    while guess_no < word_length:
        display_guess_matrix(guess_no, word_length, scores)
        guess_range = GUESS_INDEX_TUPLE[word_length - 6][guess_no - 1]

        while True:
            guess = input(f'Now enter Guess {guess_no}: ')

            if len(guess) == guess_range[1] - guess_range[0] + 1:
                break

        score = compute_value_for_guess(word, guess_range[0], guess_range[1], guess)
        scores += (score,)
        guess_no += 1

    display_guess_matrix(guess_no, word_length, scores)
    final_guess = input("Now enter your final guess. i.e. guess the whole word: ")

    if final_guess == word:
        print("You have guessed the word correctly. Congratulations.")
    else:
        print(f'Your guess was wrong. The correct word was \"{word}\"')


def main():
    """
    Handles top-level interaction with user.
    """
    # Write the code for your main function here
    print(WELCOME)
    while True:
        user_input = input(INPUT_ACTION);
        if user_input is "s":
            game()
            break
        elif user_input is "h":
            print(HELP)
            game()
            break
        elif user_input is "q":
            break
        else:
            print(INVALID)





if __name__ == "__main__":
    main()

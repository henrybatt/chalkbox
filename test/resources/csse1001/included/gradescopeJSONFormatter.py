import json
import sys
import pprint


"""
gradescopeJSONFormatter.py

Takes the json output from testrunner.py (CSSE1001 test runner) and converts the output into the 
json format for gradescope

"""

__author__  = "Ella de Lore"
__date__    = "30/09/2020"



def main():

    #check args
    if len(sys.argv) != 2:
        if len(sys.argv) < 2:
            print("Not enough arguments given")
            return 1
        else:
            print("Too many arguments given")
            return 1


    #open and read file to dict
    file = open(sys.argv[1])
    data = json.load(file)
    file.close()

    results_dict = {}

    #set output
    results_dict["output"] = data["output"]

    #set total score
    results_dict["score"] = data["test"]["passed"]

    #set up individual tests
    results_dict["tests"] = []
    tests = list(data["test"]["results"].keys())

    #format test
    for test in tests:
        results_dict["tests"].append(formatTest(data, test))

    pp = pprint.PrettyPrinter(indent=4)
    pp.pprint(results_dict)

    file = open(sys.argv[1], "w")
    file.write(json.dumps(results_dict))
    file.close()



def formatTest(data, current_test):
    test_data = {}

    test_data["name"] = current_test

    sub_tests = list(data["test"]["results"][current_test])
    output = ""
    score = 0
    max_score = len(sub_tests)
    for index, test in enumerate(sub_tests):
        result = data["test"]["results"][current_test][test]
        output += "{}. {} : {}\n".format(index + 1, test, result)

        if result == "+":
            score += 1

    test_data["score"] = score
    test_data["max_score"] = max_score
    test_data["output"] = output

    return test_data


if __name__ == "__main__":
    main()

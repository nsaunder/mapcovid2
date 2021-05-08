import requests
from bs4 import BeautifulSoup
import json
from os.path import dirname, join
import pickle
import datetime
import os
import math

def get_weather():
    URL = 'https://api.weatherbit.io/v2.0/current?key=a0ef2ad051944a5a90807e09fad0f8c6&units=I&city=Los Angeles&country=US'
    page = requests.get(URL)
    json_data = json.loads(page.text)
    return (json_data['data'][0]['weather']['description'] + "_" + str(json_data['data'][0]['temp']))

def create_new_file():
    #[GRABBING OLD DATA -- BASELINE DATA IS A DICTIONARY]
    dt = datetime.datetime.today()
    day = dt.day
    filename = join(dirname(__file__), "baseline_data")
    with open(filename, 'rb') as f:
        baseline_data = pickle.load(f)

    #[NEW DATA]
    try:
        URL = 'http://publichealth.lacounty.gov/media/coronavirus/locations.htm'
        page = requests.get(URL)
        soup = BeautifulSoup(page.content, 'html.parser')
        table_results = soup.find_all("tbody")[0]
        expanded_list = []
        expanded_results = table_results.find_all("tr")
        for item in expanded_results:
            expanded_list.append(item.find_all("td"))
        clean_list = []
        for item in expanded_list:
            temp = []
            for entry in item:
                temp.append(entry.get_text())
            clean_list.append(temp)
        parsed_list = []
        for item in clean_list:
            if "City of" in item[0]:
                parsed_list.append(item)
        new_data = {}
        for item in parsed_list:
            temp = item[0][8:]
            if (temp[-1] == '*'):
                temp = temp[:-1]
            new_data[temp] = [int(item[1]), int(item[3])]

        #[CITY DATA FILE]
        city_filename = join(dirname(__file__), "city_data")
        with open(city_filename) as f:
            city_data = json.load(f)
        final_comparison = []
        for i in range(len(city_data)):
            if city_data[i]["city_name"] in baseline_data:
                temp = {"city_name": city_data[i]["city_name"]}
                temp["center_lat"] = city_data[i]["center_lat"]
                temp["center_long"] = city_data[i]["center_long"]
                temp["radius"] = city_data[i]["radius"]
                temp["new_cases"] = int(math.ceil((new_data[city_data[i]["city_name"]][0] - baseline_data[city_data[i]["city_name"]][0])/float(((30 + day) - 11))))
                temp["new_deaths"] = int(math.ceil((new_data[city_data[i]["city_name"]][1] - baseline_data[city_data[i]["city_name"]][1])/float(((30 + day) - 11))))
                temp["total_cases"] = int((new_data[city_data[i]["city_name"]][0]))
                temp["total_deaths"] = int((new_data[city_data[i]["city_name"]][1]))
                final_comparison.append(temp)
            else:
                final_comparison.append(city_data[i])

        #print (final_comparison)
        with open(os.environ["HOME"] + "/final_city_data.json", 'w') as outfile:
            json.dump(final_comparison, outfile)
    except:
        raise Exception("Airplane mode has been activated.")

        #WRITTEN TO /storage/emulated/0/final_city_data.json









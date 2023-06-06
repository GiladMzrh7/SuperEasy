using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Bagrut2023HaChana
{
    public class Flashlight
    {
        private string model;
        private int price;

        public Flashlight(string model, int price)
        {
            this.model = model;
            this.price = price;
        }
        
        //set and get methods to both atts
           
        public string GetModel() { return model; }
        public int GetPrice() { return price; }
        public void SetPrice(int price) { this.price = price; }
        
        public void SetModel(string model) { this.model = model;}



    }

    class Car
    {
        private string licenseNum;
        private bool had_accident;
        private int price;

        public Car(string liceseN, bool acc, int p)
        {
            licenseNum = liceseN;
            had_accident = acc;
            price = p;
        }

        public bool Range(int min, int max)
        {
            return min < price && price < max;
        }

        public string GetLicense() { return licenseNum; }
        public bool GetAccident() { return had_accident; }

    }
    
    class AllCars
    {
        private Car[] cars;
        private int num;

        public AllCars(int max)
        {
            cars = new Car[max];
            num = 0;
        }

        public bool AddCar(Car car)
        {
            if (num >= cars.Length || car == null)
                return false;

            cars[num] = car;
            num++;
            return true;
        }

        public void Print(int min, int max)
        {
            foreach (Car c in cars)
            {
                if(!c.GetAccident() && c.Range(min,max))
                    Console.WriteLine(c.GetLicense());
            }
        }

    }

    class Range
    {
        public int min;
        public int max;
        public Range(int min, int max)
        {
            this.min = min;
            this.max = max;
        }
        public int GetMax() { return max; }
        public int GetMin() { return min; }

    }
}

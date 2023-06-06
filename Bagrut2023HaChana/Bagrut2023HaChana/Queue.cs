using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Bagrut2023HaChana
{
    class Queues<T>
    {
        private Node<T> first;
        private Node<T> last;
        

        public Queues()
        {
            first = null;
            last = null;
        }

        public bool IsEmpty() { return first == null; }

        public T GetHead() { return first.GetValue(); }

        public void Insert(T x)
        {
            if (last != null)
            {
                last.SetNext(new Node<T>(x));
                last = last.GetNext();
            }
            else
            {
                last = new Node<T>(x);
                first = last;
            }
        }

        public T remove()
        {
            T x = first.GetValue();
            first = first.GetNext();

            if (first == null)
            {
                last = null;
            }
            return x;
        }

        public override string ToString()
        {
            string s = "";
            Node<T> a = first;
            while (a != null)
            {
                s += $"{a.GetValue()} ,";
                a = a.GetNext();
            }
            return s;
        }

    }
}

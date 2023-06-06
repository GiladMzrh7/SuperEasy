using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Bagrut2023HaChana
{
    class Node<T>
    {
        private T value;
        private Node<T> next;

        public Node(T value, Node<T> next)
        {
            this.value = value;
            this.next = next;
        }

        public Node(T value)
        {
            this.value = value;
            this.next = null;
        }

        public T GetValue() { return value; }
        public void SetValue(T v) { value = v; }
        public Node<T> GetNext() { return next; }
        public void SetNext(Node<T> a) { next = a; }
        public bool HasNext() { return next != null; }
        public override string ToString() { return " value: " + value + " Next: " + next; }

    }

}

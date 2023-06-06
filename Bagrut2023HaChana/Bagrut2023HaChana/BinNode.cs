using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Bagrut2023HaChana
{
    class BinNode<T>
    {
        T value;
        BinNode<T> left;
        BinNode<T> right;

        public BinNode(T value)
        {
            this.value = value;
        }

        public BinNode(BinNode<T> left,T val, BinNode<T> right)
        {
            this.left = left;
            this.right = right;
            value = val;
        }

        public void SetRight(BinNode<T> a) { right = a; }
        public void SetLeft(BinNode<T> a) { left = a; }
        public void SetValue(T val) { value = val; }

        public T GetValue() { return value; }
        public BinNode<T> GetRight() { return right; }
        public BinNode<T> GetLeft() { return left; }

        public bool HasLeft() { return left != null; }
        public bool HasRight() { return right != null; }

    }
}

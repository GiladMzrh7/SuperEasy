using Bagrut2023HaChana;

namespace Bagrut2023HaChana
{
    class TreeUtils
    {


        public static void PrintPreOrder<T>(BinNode<T> tr)
        {
            if (tr == null)
            {
                return;
            }
            Console.Write(tr + " ");
            PrintPreOrder<T>(tr.GetLeft());
            PrintPreOrder<T>(tr.GetRight());
        }

        public static void PrintInOrder<T>(BinNode<T> tr)
        {
            if (tr == null)
            {
                return;
            }

            PrintInOrder<T>(tr.GetLeft());
            Console.Write(tr + " ");
            PrintInOrder<T>(tr.GetRight());
        }

        public static void PrintPostOrder<T>(BinNode<T> tr)
        {
            if (tr == null)
            {
                return;
            }

            PrintPostOrder<T>(tr.GetLeft());
            PrintPostOrder<T>(tr.GetRight());
            Console.Write(tr + " ");
        }

        public static void PrintLevelOrder<T>(BinNode<T> tr)
        {
            Queues<BinNode<T>> q = new Queues<BinNode<T>>();
            q.Insert(tr);
            while(!q.IsEmpty())
            {
                BinNode<T> temp = q.remove();
                Console.Write($"{temp}, ");
                if (temp.GetLeft() != null)
                    q.Insert(temp.GetLeft());
                if (temp.GetRight() != null)
                    q.Insert(temp.GetRight());
            }
        }

        public static int NumNodes<T>(BinNode<T> t)
        {
            if (t == null)
            {
                return 0;
            }
            return NumNodes<T>(t.GetLeft()) + NumNodes<T>(t.GetRight()) + 1;
        }

        public static int SumOfNodes(BinNode<int> t)
        {
            if (t == null)
            {
                return 0;
            }
            return SumOfNodes(t.GetLeft()) + SumOfNodes(t.GetRight()) + t.GetValue();
        }

        public static int CountEven(BinNode<int> t)
        {
            int temp = 0;
            if (t == null)
            {
                return 0;
            }
            if (t.GetValue() % 2 == 0)
                temp = 1;

            return CountEven(t.GetLeft()) + CountEven(t.GetRight()) + temp;
        }
        public static int NumPos(BinNode<int> t)
        {
            if (t == null)
            {
                return 0;
            }

            if (t.GetValue() > 0)
            {
                return NumPos(t.GetLeft()) + NumPos(t.GetRight()) + 1;
            }
            else

                return NumPos(t.GetLeft()) + NumPos(t.GetRight());
        }



        private static bool IsLeaf<T>(BinNode<T> t)
        {
            return (t.GetRight() == null && t.GetLeft() == null);
        }

        public static int CountLeaves<T>(BinNode<T> t)
        {
            int temp = 0;
            if (t == null)
            {
                return 0;
            }
            if (IsLeaf<T>(t))
            {
                temp = 1;
            }
            return CountLeaves<T>(t.GetLeft()) + CountLeaves<T>(t.GetRight()) + temp;
        }


        public static int Get_max(BinNode<int> t)
        {

            if (t == null)
            {
                return int.MinValue;
            }

            return Math.Max(t.GetValue(), Math.Max(Get_max(t.GetRight()), Get_max(t.GetLeft())));
        }

        public static int NumLeftSons(BinNode<int> t)
        {

            if (t == null)
            {
                return 0;
            }

            return NumLeftSons(t.GetLeft()) + 1;
        }

        public static int SumRight(BinNode<int> t)
        {
            int b = 0;
            if (t == null)
            {
                return 0;
            }
            if (t.GetRight() != null)
            {
                b = SumRight(t.GetRight()) + t.GetRight().GetValue() + SumRight(t.GetLeft());
            }
            return b;
        }

        public static bool IsExist(int val, BinNode<int> t)
        {
            if (t == null)
                return false;

            if (t.GetValue() == val)
                return true;

            return IsExist(val, t.GetLeft()) || IsExist(val, t.GetRight());
        }

        public static void SmallerSon(BinNode<int> t)
        {
            if (t == null)
                Console.WriteLine("");
            if (t.GetRight() != null || t.GetLeft() != null)
                if (t.GetRight() != null && t.GetRight().GetValue() < t.GetValue())
                {
                    Console.Write(t.GetRight() + ",");
                    SmallerSon(t.GetRight());
                }

            if (t.GetLeft() != null && t.GetLeft().GetValue() < t.GetValue())
            {
                Console.Write(t.GetLeft() + ",");
                SmallerSon(t.GetLeft());
            }

            if (t.GetRight() != null)
            {

                SmallerSon(t.GetRight());
            }

            if (t.GetLeft() != null)
            {

                SmallerSon(t.GetLeft());
            }
        }

        public static void BiggerThenSon(BinNode<int> t)
        {
            if (t == null)
                Console.WriteLine("");
            if (t.GetRight() != null && t.GetLeft() != null)
            {
                if (t.GetRight().GetValue() < t.GetValue())
                {
                    Console.Write(t + ",");
                    BiggerThenSon(t.GetRight());
                }
                else
                    BiggerThenSon(t.GetRight());

                if (t.GetLeft().GetValue() < t.GetValue())
                {
                    Console.Write(t + ",");
                    BiggerThenSon(t.GetLeft());
                }
                else
                    BiggerThenSon(t.GetLeft());

            }



        }

        public static int GetHeight<T>(BinNode<T> tr)
        {
            if (tr == null)
                return -1;

            return 1 + Math.Max(GetHeight(tr.GetLeft()), GetHeight(tr.GetRight()));
        }

        public static bool IsBalanced<T>(BinNode<T> tr)
        {
            if(Math.Abs(GetHeight(tr.GetRight()) - GetHeight(tr.GetLeft())) > 1)
                return false;

            if (tr == null)
                return true;

            return IsBalanced(tr.GetLeft()) && IsBalanced(tr.GetRight());
        }


        public static int NumOfOnlySuns<T>(BinNode<T> tr)
        {
            if (tr == null)
                return 0;
            if (tr.GetRight() == null && tr.GetLeft() != null || tr.GetRight() != null && tr.GetLeft() == null)
                return 1 + NumOfOnlySuns(tr.GetLeft()) + NumOfOnlySuns(tr.GetRight());
            return NumOfOnlySuns(tr.GetLeft()) + NumOfOnlySuns(tr.GetRight());
        }

        public static int NumOfDoubleParents<T>(BinNode<T> tr)
        {
            if (tr == null)
                return 0;
            if (tr.GetRight() != null && tr.GetLeft() != null)
                return 1 + NumOfDoubleParents(tr.GetLeft()) + NumOfDoubleParents(tr.GetRight());
            return NumOfDoubleParents(tr.GetLeft()) + NumOfDoubleParents(tr.GetRight());
        }

        public static int NumOfHigher(int x, BinNode<int> tr)
        {
            if (tr == null)
                return 0;
            if (tr.GetValue() > x)
                return 1 + NumOfHigher(x, tr.GetLeft()) + NumOfHigher(x, tr.GetRight());
            return NumOfHigher(x, tr.GetLeft()) + NumOfHigher(x, tr.GetRight());
        }

        public static void PrintLevel<T>(BinNode<T> t, int level)
        {
            if(level == 0 && t != null)
            {
                Console.Write($"{t.GetValue()}, ");
                return;
            }
                

            if (t == null)
                return;
            PrintLevel(t.GetLeft(), level - 1);
            PrintLevel(t.GetRight(), level - 1);

        }

        public static void AddSimillarBrother<T>(BinNode<T> t)
        {
            if (t == null)
                return;
            if (t.GetRight() == null && t.GetLeft() != null)
                t.SetRight(new BinNode<T>(t.GetLeft().GetValue()));
            else if(t.GetRight() != null && t.GetLeft() == null)
                t.SetLeft(new BinNode<T>(t.GetRight().GetValue()));
            AddSimillarBrother(t.GetLeft());
            AddSimillarBrother(t.GetRight());
        }

        public static void RemoveLeaves<T>(BinNode<T> t)
        {
            if (t == null)
                return;
            if(t.GetRight().GetLeft() == null && t.GetRight().GetRight() == null)
                t.SetRight(null);              
            if (t.GetLeft().GetLeft() == null && t.GetLeft().GetRight() == null)
                t.SetLeft(null);

            RemoveLeaves(t.GetLeft());
            RemoveLeaves(t.GetRight());
        }


        public static void PrintAllBigParents(BinNode<int> t)
        {
            if (t == null)
                return;
            if(t.GetLeft() != null && t.GetRight() != null && t.GetValue() > t.GetRight().GetValue() && t.GetValue() > t.GetLeft().GetValue())
                Console.WriteLine(t.GetValue());
            PrintAllBigParents(t.GetRight());
            PrintAllBigParents(t.GetLeft());
        }

        public static int GetAmountOfEqualSons(BinNode<int> t)
        {
            if (t == null)
                return 0;
            if (t.GetLeft() != null && t.GetRight() != null && t.GetRight().GetValue() == t.GetLeft().GetValue())
                return 1 + GetAmountOfEqualSons(t.GetRight()) + GetAmountOfEqualSons(t.GetLeft());               
            return GetAmountOfEqualSons(t.GetRight()) + GetAmountOfEqualSons(t.GetLeft());
        }
        

        public static int GetAmountOfSmallerSons(BinNode<int> t)
        {
            int amount = 0;
            if (t == null)
                return 0;
            if (t.GetRight() != null && t.GetValue() > t.GetRight().GetValue())
                amount++;
            if (t.GetLeft() != null && t.GetValue() > t.GetLeft().GetValue())
                amount++;
            return amount + GetAmountOfSmallerSons(t.GetLeft()) + GetAmountOfSmallerSons(t.GetRight());

        }

        public static bool IsEven(BinNode<int> t)
        {
            if (t == null)
                return true;
            if (t.GetValue() % 2 == 0)
                return IsEven(t.GetRight()) && IsEven(t.GetLeft()) && true;
            else
                return IsEven(t.GetRight()) && IsEven(t.GetLeft()) && false;
        }

        public static bool IsSumTree(BinNode<int> t)
        {
            if (t == null)
                return true;
            if (t.GetValue() == SumOfNodes(t))
                return IsSumTree(t.GetRight()) && IsSumTree(t.GetLeft());
            return false;
        }
    }
}

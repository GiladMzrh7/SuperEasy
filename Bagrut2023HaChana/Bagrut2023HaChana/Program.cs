using Bagrut2023HaChana;


Queues<int> a = new Queues<int>();
a.Insert(1);
a.Insert(9);
a.Insert(6);
Console.WriteLine(ToNumber(a));
string c = "aaa";
Console.WriteLine(c[1]);

static int GetScore(BinNode<int> a)
{
    if (isLeaf(a))
    {
        return 1;
    }
    else if (HasTwo(a))
    {
        return Math.Max(a.GetLeft().GetValue(), a.GetRight().GetValue()) + GetScore(a.GetLeft()) + GetScore(a.GetRight());
    }

    else if (a.HasRight())
        return a.GetRight().GetValue() /2 + GetScore(a.GetRight());
    
    else
        return a.GetLeft().GetValue() + GetScore(a.GetLeft());
}

static bool HasTwo(BinNode<int> a)
{
    return a.HasLeft() && a.HasRight();
}

static bool isLeaf(BinNode<int> a)
{
    return !a.HasLeft() && !a.HasRight();
}

static void SpilledOn<T>(Queues<T> a, Queues<T> b)
{
    while (!a.IsEmpty())
    {
        b.Insert(a.remove());
    }
}

static int ToNumber(Queues<int> q)
{   
    Queues<int> ret = new Queues<int>();
    SpilledOn(q, ret);
    int num = ret.remove();
    while (!ret.IsEmpty())
    {
        num *= 10;
        num += ret.remove();
    }
    return num;
}

static int BigNumber(Node<Queues<int>> node)
{
    Node<Queues<int>> pointer = node;
    int max = int.MinValue;
    while (pointer.HasNext())
    {
        int result = ToNumber(pointer.GetValue());
        if (result > max)
            max = result;

        pointer = pointer.GetNext();
    }
    return max;
}

static void threeFlashlights(Flashlight[] s, int total)
{
    //2*o(N^3)

    int[,,] prices = new int[s.Length, s.Length, s.Length];
    for (int i = 0; i < s.Length; i++)
    {
        for (int j = 0; j < s.Length; j++)
        {
            for (int x = 0; x < s.Length; x++)
            {
                if (i == j || j == x || i == x)
                {
                    continue;
                }
                prices[i, j, x] = s[i].GetPrice() + s[j].GetPrice() + s[x].GetPrice();
            }
        }
    }

    for (int i = 0; i < s.Length; i++)
    {
        for (int j = 0; j < s.Length; j++)
        {
            for (int x = 0; x < s.Length; x++)
            {

                if (prices[i,j,x] == total)
                {
                    Console.WriteLine(s[i].GetModel() + " " + s[j].GetModel() + " " + s[x].GetModel());
                }

            }
        }
    }
}

static int Exact(string[] arr, int num)
{
    int amn = 0;
    foreach (string s in arr)
    { 
        if (s.Length == num)
            amn++;
    }
    return amn;
}



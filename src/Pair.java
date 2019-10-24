public class Pair<K, V> {
	K left;
	V right;

	public Pair (K left, V right) {
		this.left = left;
		this.right = right;
	}

	public K getLeft() { return this.left; }
	
	public V getRight() { return this.right; }
}
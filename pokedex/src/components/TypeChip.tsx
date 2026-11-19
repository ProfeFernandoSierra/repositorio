import type { Category } from '../types';

export default function TypeChip({ category }: { category: Category }) {
  return (
    <span className="type" style={{ background: category.color }}>
      {category.name}
    </span>
  );
}

import { useParams } from 'react-router-dom'
import { unshorten } from '../services/short-id'

const useIdParam = () => {
  const { id } = useParams<{ id: string }>()
  if (id === undefined) {
    throw new Error("Path parameter 'id' not found")
  }
  return unshorten(id)
}

export default useIdParam

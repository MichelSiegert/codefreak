import { Alert, Card, Icon, Steps } from 'antd'
import React, { useEffect, useState } from 'react'
import {
  IdeType,
  useCheckIdeLivelinessLazyQuery,
  useStartIdeMutation
} from '../generated/graphql'
import { extractErrorMessage } from '../services/codefreak-api'

const { Step } = Steps

const LaunchIdeSteps: React.FC<{
  type: IdeType
  id: string
  onReady: (url: string) => void
}> = ({ onReady, type, id }) => {
  const [currentStep, setCurrentStep] = useState(0)
  const [ideUrl, setIdeUrl] = useState<string>()
  const [error, setError] = useState<string>()

  const [startIde] = useStartIdeMutation({
    variables: { type, id },
    onError: e => setError(extractErrorMessage(e))
  })
  const [
    checkIdeLiveliness,
    { data, loading }
  ] = useCheckIdeLivelinessLazyQuery({
    variables: { type, id },
    fetchPolicy: 'network-only'
  })

  useEffect(() => {
    if (error || loading || !ideUrl) return
    if (data?.checkIdeLiveliness === true) {
      setCurrentStep(2)
      onReady(ideUrl)
    } else {
      window.setTimeout(() => {
        checkIdeLiveliness()
      }, 1000)
    }
  }, [onReady, error, ideUrl, data, loading, checkIdeLiveliness])

  useEffect(() => {
    startIde().then(res => {
      if (res && res.data) {
        setCurrentStep(1)
        setIdeUrl(res.data.startIde)
        checkIdeLiveliness()
      }
    })
  }, [startIde, checkIdeLiveliness])

  const icon = (iconType: string, step: number) => (
    <Icon type={step === currentStep && !error ? 'loading' : iconType} />
  )

  return (
    <Card style={{ width: '100%', maxWidth: 800 }}>
      {error ? (
        <Alert message={error} type="error" style={{ marginBottom: 16 }} />
      ) : null}
      <Steps current={currentStep} status={error ? 'error' : 'process'}>
        <Step title="Launch Container" icon={icon('rocket', 0)} />
        <Step title="Wait for IDE Startup" icon={icon('cloud', 1)} />
        <Step title="Redirect" icon={icon('forward', 2)} />
      </Steps>
    </Card>
  )
}

export default LaunchIdeSteps
